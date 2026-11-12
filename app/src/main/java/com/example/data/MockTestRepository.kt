package com.example.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.R
import com.example.firebase.OperationType
import com.example.firebase.handleFirestoreError
import com.example.model.ActiveTestState
import com.example.model.AdmissionUnit
import com.example.model.AppLanguage
import com.example.model.MockQuestion
import com.example.model.QuestionSourceType
import com.example.model.SubjectSpec
import com.example.model.SubmittedTestResult
import com.example.model.TestMode
import com.example.model.TestRecord
import com.example.model.TestReviewItem
import com.example.model.UniversityConfig
import com.example.model.VerifiedQuestion
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

val Context.mockTestDataStore: DataStore<Preferences> by preferencesDataStore(name = "mock_test_prefs")

object ScoringEngine {
    fun evaluateTest(
        userId: String,
        session: ActiveTestState
    ): SubmittedTestResult {
        var correctCount = 0
        var wrongCount = 0
        var unansweredCount = 0

        val reviewItems = session.questions.mapIndexed { index, question ->
            val selected = session.selectedAnswers[index].orEmpty()
            when {
                selected.isBlank() -> unansweredCount++
                selected == question.correctOption -> correctCount++
                else -> wrongCount++
            }
            TestReviewItem(
                id = "item_$index",
                questionIndex = index.toLong(),
                subject = question.subject,
                topic = question.topic,
                questionBn = question.questionBn,
                optionA = question.optionA,
                optionB = question.optionB,
                optionC = question.optionC,
                optionD = question.optionD,
                correctOption = question.correctOption,
                selectedOption = selected,
                explanationBn = question.explanationBn,
                isAiGenerated = question.isAiGenerated,
                sourceInfo = question.sourceInfo
            )
        }

        val totalQuestions = session.questions.size
        val attemptedCount = correctCount + wrongCount
        val totalScore = (correctCount * session.positiveMark) -
            (wrongCount * session.negativeMark) +
            (unansweredCount * session.unansweredMark)
        val maxScore = (totalQuestions * session.positiveMark).coerceAtLeast(1.0)
        val accuracy = if (attemptedCount > 0) {
            (correctCount.toDouble() / attemptedCount.toDouble()) * 100.0
        } else {
            0.0
        }
        val timeTaken = (session.totalDurationSeconds - session.remainingSeconds)
            .coerceIn(0, session.totalDurationSeconds)

        val record = TestRecord(
            id = UUID.randomUUID().toString(),
            userId = userId,
            mode = session.mode.key,
            titleEn = session.titleEn,
            titleBn = session.titleBn,
            universityId = session.universityId,
            unitId = session.unitId,
            sessionYear = session.sessionYear,
            sourceType = session.sourceType.key,
            totalQuestions = totalQuestions.toLong(),
            attemptedCount = attemptedCount.toLong(),
            correctCount = correctCount.toLong(),
            wrongCount = wrongCount.toLong(),
            unansweredCount = unansweredCount.toLong(),
            totalScore = totalScore,
            maxPossibleScore = maxScore,
            accuracyPercent = accuracy,
            durationSeconds = session.totalDurationSeconds.toLong(),
            timeTakenSeconds = timeTaken.toLong(),
            positiveMark = session.positiveMark,
            negativeMark = session.negativeMark,
            unansweredMark = session.unansweredMark
        )
        return SubmittedTestResult(record = record, items = reviewItems)
    }
}

class MockTestRepository(
    private val db: FirebaseFirestore,
    private val dataStore: DataStore<Preferences>? = null
) {
    constructor(context: Context) : this(
        db = FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        dataStore = context.applicationContext.mockTestDataStore
    )

    private val auth get() = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // --- Language Preference (DataStore + Firestore) ---
    fun observeLanguage(): Flow<AppLanguage> {
        val ds = dataStore ?: return kotlinx.coroutines.flow.flowOf(AppLanguage.EN)
        return ds.data.map { prefs ->
            AppLanguage.fromCode(prefs[KEY_LANGUAGE])
        }
    }

    suspend fun saveLanguage(language: AppLanguage, userId: String?, displayName: String?) {
        dataStore?.edit { prefs ->
            prefs[KEY_LANGUAGE] = language.code
        }
        if (userId != null && auth.currentUser?.uid == userId) {
            upsertUserProfile(userId, displayName ?: "Student", language)
        }
    }

    suspend fun upsertUserProfile(userId: String, displayName: String, language: AppLanguage) {
        val docRef = db.collection("users").document(userId)
        try {
            val existing = docRef.get().await()
            if (existing.exists()) {
                docRef.update(
                    mapOf(
                        "displayName" to displayName.take(100).ifBlank { "Student" },
                        "preferredLanguage" to language.code,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "userId" to userId,
                        "displayName" to displayName.take(100).ifBlank { "Student" },
                        "preferredLanguage" to language.code,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
        }
    }

    // --- Admin Access Check ---
    suspend fun checkIsAdmin(userId: String): Boolean {
        val user = auth.currentUser
        if (user != null && user.isEmailVerified && user.email == BOOTSTRAP_ADMIN_EMAIL) {
            return true
        }
        val docRef = db.collection("admins").document(userId)
        return try {
            val snap = docRef.get().await()
            snap.exists() && snap.getString("role") == "admin"
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Non-admin users will get PERMISSION_DENIED or not-found gracefully
            false
        }
    }

    // --- University Catalogue (Merged Defaults + Firestore Admin Overrides) ---
    fun observeUniversities(): Flow<List<UniversityConfig>> {
        val path = "universities"
        return db.collection(path)
            .whereEqualTo("visibility", "public")
            .snapshots()
            .map { snapshot ->
                val cloudList = snapshot.documents.mapNotNull { parseUniversityDoc(it) }
                if (cloudList.isEmpty()) {
                    AdmissionCatalogDefaults.universities
                } else {
                    val cloudById = cloudList.associateBy { it.id }
                    val merged = AdmissionCatalogDefaults.universities.map { defaultUni ->
                        cloudById[defaultUni.id] ?: defaultUni
                    }.toMutableList()
                    cloudList.filter { cloud -> merged.none { it.id == cloud.id } }
                        .forEach { merged.add(it) }
                    merged
                }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (firestoreError != null) {
                    handleFirestoreError(firestoreError, OperationType.LIST, path)
                }
                emit(AdmissionCatalogDefaults.universities)
            }
    }

    // --- Verified Past Questions ---
    fun observeVerifiedQuestions(): Flow<List<VerifiedQuestion>> {
        val path = "verified_questions"
        return db.collection(path)
            .whereEqualTo("visibility", "public")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    VerifiedQuestion::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (firestoreError != null) {
                    handleFirestoreError(firestoreError, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun fetchVerifiedQuestionsForTest(
        universityId: String,
        unitId: String,
        subjectCounts: Map<String, Int>
    ): List<MockQuestion> {
        val path = "verified_questions"
        val docs = try {
            db.collection(path)
                .whereEqualTo("visibility", "public")
                .whereEqualTo("universityId", universityId)
                .whereEqualTo("unitId", unitId)
                .get()
                .await()
                .toObjects(VerifiedQuestion::class.java)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            throw e
        }

        val collected = mutableListOf<MockQuestion>()
        for ((subject, needed) in subjectCounts) {
            if (needed <= 0) continue
            val matching = docs.filter {
                it.subject.equals(subject, ignoreCase = true) ||
                    it.subject.contains(subject, ignoreCase = true)
            }.shuffled().take(needed)
            matching.forEach { q ->
                collected.add(
                    MockQuestion(
                        id = q.id,
                        index = collected.size,
                        subject = q.subject,
                        topic = q.topic,
                        questionBn = q.questionBn,
                        optionA = q.optionA,
                        optionB = q.optionB,
                        optionC = q.optionC,
                        optionD = q.optionD,
                        correctOption = q.correctOption,
                        explanationBn = q.explanationBn,
                        isAiGenerated = false,
                        sourceInfo = "${q.sourceLabel} (${q.examYear})"
                    )
                )
            }
        }
        return collected
    }

    // --- Admin Management CRUD ---
    suspend fun upsertUniversity(university: UniversityConfig): Result<Unit> {
        val uid = requireUserId()
        val id = university.id.ifBlank {
            university.abbreviation.lowercase().replace("[^a-z0-9_\\-]".toRegex(), "_")
        }
        val docRef = db.collection("universities").document(id)
        return try {
            val existing = docRef.get().await()
            val unitsList = university.units.take(20).map { unit ->
                mapOf(
                    "id" to unit.id,
                    "nameEn" to unit.nameEn,
                    "nameBn" to unit.nameBn,
                    "patternVerified" to unit.patternVerified,
                    "sessionYear" to unit.sessionYear,
                    "totalQuestions" to unit.totalQuestions,
                    "durationMinutes" to unit.durationMinutes,
                    "positiveMark" to unit.positiveMark,
                    "negativeMark" to unit.negativeMark,
                    "unansweredMark" to unit.unansweredMark,
                    "totalMarks" to unit.totalMarks,
                    "sourceReference" to unit.sourceReference,
                    "verificationDate" to unit.verificationDate,
                    "notesEn" to unit.notesEn,
                    "notesBn" to unit.notesBn,
                    "subjects" to unit.subjects.map { sub ->
                        mapOf(
                            "id" to sub.id,
                            "nameEn" to sub.nameEn,
                            "nameBn" to sub.nameBn,
                            "defaultCount" to sub.defaultCount,
                            "isCompulsory" to sub.isCompulsory
                        )
                    }
                )
            }
            val payload = hashMapOf<String, Any>(
                "nameEn" to university.nameEn.take(160),
                "nameBn" to university.nameBn.take(160),
                "abbreviation" to university.abbreviation.take(24),
                "category" to university.category,
                "sessionYear" to university.sessionYear.take(32),
                "sourceReference" to university.sourceReference.take(300),
                "verificationDate" to university.verificationDate.take(40),
                "isPatternVerified" to university.isPatternVerified,
                "units" to unitsList,
                "visibility" to "public",
                "updatedBy" to uid,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (existing.exists()) {
                docRef.update(payload).await()
            } else {
                payload["createdAt"] = FieldValue.serverTimestamp()
                docRef.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteUniversity(universityId: String): Result<Unit> {
        requireUserId()
        val docRef = db.collection("universities").document(universityId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun seedDefaultUniversitiesToCloud(): Result<Unit> {
        return try {
            for (uni in AdmissionCatalogDefaults.universities) {
                val res = upsertUniversity(uni)
                if (res.isFailure) return res
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun upsertVerifiedQuestion(question: VerifiedQuestion): Result<String> {
        val uid = requireUserId()
        val id = question.id.ifBlank { "vq_${UUID.randomUUID().toString().replace("-", "").take(16)}" }
        val docRef = db.collection("verified_questions").document(id)
        return try {
            val existing = docRef.get().await()
            val payload = hashMapOf<String, Any>(
                "universityId" to question.universityId.take(128),
                "unitId" to question.unitId.take(64),
                "examYear" to question.examYear.take(32),
                "sourceLabel" to question.sourceLabel.take(160),
                "subject" to question.subject.take(80),
                "topic" to question.topic.take(120),
                "difficulty" to question.difficulty,
                "questionBn" to question.questionBn.take(1500),
                "optionA" to question.optionA.take(500),
                "optionB" to question.optionB.take(500),
                "optionC" to question.optionC.take(500),
                "optionD" to question.optionD.take(500),
                "correctOption" to question.correctOption,
                "explanationBn" to question.explanationBn.take(2000),
                "visibility" to "public",
                "verifiedBy" to uid,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (existing.exists()) {
                docRef.update(payload).await()
            } else {
                payload["createdAt"] = FieldValue.serverTimestamp()
                docRef.set(payload).await()
            }
            Result.success(id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteVerifiedQuestion(questionId: String): Result<Unit> {
        requireUserId()
        val docRef = db.collection("verified_questions").document(questionId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // --- Test History & Review Items ---
    fun observeTestHistory(userId: String): Flow<List<TestRecord>> {
        val path = "users/$userId/test_records"
        return db.collection("users").document(userId).collection("test_records")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    TestRecord::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (firestoreError != null && auth.currentUser?.uid == userId) {
                    handleFirestoreError(firestoreError, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun getTestReviewItems(userId: String, recordId: String): Result<List<TestReviewItem>> {
        val path = "users/$userId/test_records/$recordId/items"
        return try {
            val items = db.collection("users").document(userId)
                .collection("test_records").document(recordId)
                .collection("items")
                .get()
                .await()
                .toObjects(TestReviewItem::class.java)
                .sortedBy { it.questionIndex }
            Result.success(items)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            Result.failure(e)
        }
    }

    suspend fun saveTestResult(result: SubmittedTestResult): Result<String> {
        val uid = requireUserId()
        val recordId = result.record.id.ifBlank { UUID.randomUUID().toString() }
        val recordRef = db.collection("users").document(uid).collection("test_records").document(recordId)
        return try {
            val recordPayload = mapOf(
                "userId" to uid,
                "mode" to result.record.mode,
                "titleEn" to result.record.titleEn.take(160),
                "titleBn" to result.record.titleBn.take(160),
                "universityId" to result.record.universityId.take(128),
                "unitId" to result.record.unitId.take(64),
                "sessionYear" to result.record.sessionYear.take(32),
                "sourceType" to result.record.sourceType,
                "totalQuestions" to result.record.totalQuestions,
                "attemptedCount" to result.record.attemptedCount,
                "correctCount" to result.record.correctCount,
                "wrongCount" to result.record.wrongCount,
                "unansweredCount" to result.record.unansweredCount,
                "totalScore" to result.record.totalScore,
                "maxPossibleScore" to result.record.maxPossibleScore,
                "accuracyPercent" to result.record.accuracyPercent,
                "durationSeconds" to result.record.durationSeconds,
                "timeTakenSeconds" to result.record.timeTakenSeconds,
                "positiveMark" to result.record.positiveMark,
                "negativeMark" to result.record.negativeMark,
                "unansweredMark" to result.record.unansweredMark,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )

            // Firestore batches support up to 500 writes; write record + items safely
            val chunks = result.items.chunked(400)
            chunks.forEachIndexed { chunkIdx, chunkItems ->
                val batch = db.batch()
                if (chunkIdx == 0) {
                    batch.set(recordRef, recordPayload)
                }
                chunkItems.forEach { item ->
                    val itemRef = recordRef.collection("items").document("item_${item.questionIndex}")
                    val itemPayload = mapOf(
                        "questionIndex" to item.questionIndex,
                        "subject" to item.subject.take(80),
                        "topic" to item.topic.take(120),
                        "questionBn" to item.questionBn.take(1500),
                        "optionA" to item.optionA.take(500),
                        "optionB" to item.optionB.take(500),
                        "optionC" to item.optionC.take(500),
                        "optionD" to item.optionD.take(500),
                        "correctOption" to item.correctOption,
                        "selectedOption" to item.selectedOption,
                        "explanationBn" to item.explanationBn.take(2000),
                        "isAiGenerated" to item.isAiGenerated,
                        "sourceInfo" to item.sourceInfo.take(200),
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                    batch.set(itemRef, itemPayload)
                }
                batch.commit().await()
            }
            clearActiveSession(uid)
            Result.success(recordId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, recordRef.path)
            Result.failure(e)
        }
    }

    // --- Active Test Progress Protection (Local DataStore + Cloud Sync) ---
    suspend fun saveActiveSession(state: ActiveTestState) {
        val json = serializeActiveState(state)
        dataStore?.edit { prefs ->
            prefs[KEY_ACTIVE_SESSION] = json
        }
        val uid = auth.currentUser?.uid ?: return
        val docRef = db.collection("users").document(uid).collection("active_session").document("current")
        try {
            val answersStringMap = state.selectedAnswers.entries.associate { it.key.toString() to it.value }
            val existing = docRef.get().await()
            if (existing.exists()) {
                docRef.update(
                    mapOf(
                        "sessionKey" to state.sessionKey.take(128),
                        "titleEn" to state.titleEn.take(160),
                        "titleBn" to state.titleBn.take(160),
                        "mode" to state.mode.key,
                        "remainingSeconds" to state.remainingSeconds,
                        "answersMap" to answersStringMap,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.set(
                    mapOf(
                        "userId" to uid,
                        "sessionKey" to state.sessionKey.take(128),
                        "titleEn" to state.titleEn.take(160),
                        "titleBn" to state.titleBn.take(160),
                        "mode" to state.mode.key,
                        "remainingSeconds" to state.remainingSeconds,
                        "answersMap" to answersStringMap,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
        }
    }

    suspend fun loadLocalActiveSession(): ActiveTestState? {
        val ds = dataStore ?: return null
        val json = ds.data.first()[KEY_ACTIVE_SESSION] ?: return null
        return deserializeActiveState(json)
    }

    suspend fun clearActiveSession(userId: String? = auth.currentUser?.uid) {
        dataStore?.edit { prefs ->
            prefs.remove(KEY_ACTIVE_SESSION)
        }
        if (userId != null && auth.currentUser?.uid == userId) {
            val docRef = db.collection("users").document(userId).collection("active_session").document("current")
            try {
                docRef.delete().await()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Non-fatal clearing active_session: ${e.message}")
            }
        }
    }

    private fun parseUniversityDoc(doc: DocumentSnapshot): UniversityConfig? {
        val data = doc.data ?: return null
        val rawUnits = (data["units"] as? List<*>)?.mapNotNull { item ->
            val uMap = item as? Map<*, *> ?: return@mapNotNull null
            val rawSubjects = (uMap["subjects"] as? List<*>)?.mapNotNull { sItem ->
                val sMap = sItem as? Map<*, *> ?: return@mapNotNull null
                SubjectSpec(
                    id = sMap["id"] as? String ?: "",
                    nameEn = sMap["nameEn"] as? String ?: "",
                    nameBn = sMap["nameBn"] as? String ?: "",
                    defaultCount = (sMap["defaultCount"] as? Number)?.toInt() ?: 10,
                    isCompulsory = sMap["isCompulsory"] as? Boolean ?: true
                )
            } ?: emptyList()

            AdmissionUnit(
                id = uMap["id"] as? String ?: "",
                nameEn = uMap["nameEn"] as? String ?: "",
                nameBn = uMap["nameBn"] as? String ?: "",
                patternVerified = uMap["patternVerified"] as? Boolean ?: false,
                sessionYear = uMap["sessionYear"] as? String ?: "2024-25",
                totalQuestions = (uMap["totalQuestions"] as? Number)?.toInt() ?: 0,
                durationMinutes = (uMap["durationMinutes"] as? Number)?.toInt() ?: 60,
                positiveMark = (uMap["positiveMark"] as? Number)?.toDouble() ?: 1.0,
                negativeMark = (uMap["negativeMark"] as? Number)?.toDouble() ?: 0.25,
                unansweredMark = (uMap["unansweredMark"] as? Number)?.toDouble() ?: 0.0,
                totalMarks = (uMap["totalMarks"] as? Number)?.toDouble() ?: 0.0,
                subjects = rawSubjects,
                sourceReference = uMap["sourceReference"] as? String ?: "",
                verificationDate = uMap["verificationDate"] as? String ?: "",
                notesEn = uMap["notesEn"] as? String ?: "",
                notesBn = uMap["notesBn"] as? String ?: ""
            )
        } ?: emptyList()

        return UniversityConfig(
            id = doc.id,
            nameEn = data["nameEn"] as? String ?: "",
            nameBn = data["nameBn"] as? String ?: "",
            abbreviation = data["abbreviation"] as? String ?: "",
            category = data["category"] as? String ?: "PUBLIC",
            sessionYear = data["sessionYear"] as? String ?: "2024-25",
            sourceReference = data["sourceReference"] as? String ?: "",
            verificationDate = data["verificationDate"] as? String ?: "",
            isPatternVerified = data["isPatternVerified"] as? Boolean ?: false,
            units = rawUnits,
            visibility = data["visibility"] as? String ?: "public",
            updatedBy = data["updatedBy"] as? String ?: ""
        )
    }

    private fun serializeActiveState(state: ActiveTestState): String {
        val root = JSONObject().apply {
            put("sessionKey", state.sessionKey)
            put("titleEn", state.titleEn)
            put("titleBn", state.titleBn)
            put("mode", state.mode.key)
            put("universityId", state.universityId)
            put("unitId", state.unitId)
            put("sessionYear", state.sessionYear)
            put("sourceType", state.sourceType.key)
            put("totalDurationSeconds", state.totalDurationSeconds)
            put("remainingSeconds", state.remainingSeconds)
            put("positiveMark", state.positiveMark)
            put("negativeMark", state.negativeMark)
            put("unansweredMark", state.unansweredMark)
            put("isOfficialPattern", state.isOfficialPattern)

            val answersObj = JSONObject()
            state.selectedAnswers.forEach { (k, v) -> answersObj.put(k.toString(), v) }
            put("selectedAnswers", answersObj)

            val qArr = JSONArray()
            state.questions.forEach { q ->
                qArr.put(JSONObject().apply {
                    put("id", q.id)
                    put("index", q.index)
                    put("subject", q.subject)
                    put("topic", q.topic)
                    put("questionBn", q.questionBn)
                    put("optionA", q.optionA)
                    put("optionB", q.optionB)
                    put("optionC", q.optionC)
                    put("optionD", q.optionD)
                    put("correctOption", q.correctOption)
                    put("explanationBn", q.explanationBn)
                    put("isAiGenerated", q.isAiGenerated)
                    put("sourceInfo", q.sourceInfo)
                })
            }
            put("questions", qArr)
        }
        return root.toString()
    }

    private fun deserializeActiveState(json: String): ActiveTestState? {
        return try {
            val root = JSONObject(json)
            val answersObj = root.getJSONObject("selectedAnswers")
            val answers = mutableMapOf<Int, String>()
            answersObj.keys().forEach { key ->
                key.toIntOrNull()?.let { idx ->
                    answers[idx] = answersObj.getString(key)
                }
            }
            val qArr = root.getJSONArray("questions")
            val questions = (0 until qArr.length()).map { i ->
                val obj = qArr.getJSONObject(i)
                MockQuestion(
                    id = obj.optString("id"),
                    index = obj.optInt("index", i),
                    subject = obj.optString("subject"),
                    topic = obj.optString("topic"),
                    questionBn = obj.optString("questionBn"),
                    optionA = obj.optString("optionA"),
                    optionB = obj.optString("optionB"),
                    optionC = obj.optString("optionC"),
                    optionD = obj.optString("optionD"),
                    correctOption = obj.optString("correctOption", "A"),
                    explanationBn = obj.optString("explanationBn"),
                    isAiGenerated = obj.optBoolean("isAiGenerated", true),
                    sourceInfo = obj.optString("sourceInfo")
                )
            }
            ActiveTestState(
                sessionKey = root.getString("sessionKey"),
                titleEn = root.getString("titleEn"),
                titleBn = root.getString("titleBn"),
                mode = TestMode.fromKey(root.getString("mode")),
                universityId = root.optString("universityId"),
                unitId = root.optString("unitId"),
                sessionYear = root.optString("sessionYear"),
                sourceType = QuestionSourceType.fromKey(root.optString("sourceType")),
                questions = questions,
                selectedAnswers = answers,
                totalDurationSeconds = root.getInt("totalDurationSeconds"),
                remainingSeconds = root.getInt("remainingSeconds"),
                positiveMark = root.getDouble("positiveMark"),
                negativeMark = root.getDouble("negativeMark"),
                unansweredMark = root.getDouble("unansweredMark"),
                isOfficialPattern = root.optBoolean("isOfficialPattern", false)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to deserialize active test session", e)
            null
        }
    }

    companion object {
        private const val TAG = "MockTestRepository"
        private const val BOOTSTRAP_ADMIN_EMAIL = "mithunmandal8873@gmail.com"
        private val KEY_LANGUAGE = stringPreferencesKey("app_language")
        private val KEY_ACTIVE_SESSION = stringPreferencesKey("active_test_session_json")
    }
}
