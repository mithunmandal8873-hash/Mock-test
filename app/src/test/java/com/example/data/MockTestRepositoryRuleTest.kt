package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.example.model.ActiveTestState
import com.example.model.AppLanguage
import com.example.model.MockQuestion
import com.example.model.QuestionSourceType
import com.example.model.TestMode
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class MockTestRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun saveTestResult_andObserveHistory_authenticatedOwner_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = MockTestRepository(firestore)
        val sessionKey = "sess_${UUID.randomUUID()}"

        val sampleQuestions = listOf(
            MockQuestion(
                id = "q1",
                index = 0,
                subject = "Biology",
                topic = "কোষ বিভাজন",
                questionBn = "কোন ধাপে ক্রসিং ওভার ঘটে?",
                optionA = "লেপ্টোটিন",
                optionB = "জাইগোটিন",
                optionC = "প্যাকাইটিন",
                optionD = "ডিপ্লোটিন",
                correctOption = "C",
                explanationBn = "মিয়োসিস-১ এর প্রোফেজ-১ এর প্যাকাইটিন উপ-পর্যায়ে ক্রসিং ওভার ঘটে।",
                isAiGenerated = true,
                sourceInfo = "AI Practice"
            ),
            MockQuestion(
                id = "q2",
                index = 1,
                subject = "Chemistry",
                topic = "পর্যায় সারণি",
                questionBn = "সর্বাধিক তড়িৎ ঋণাত্মক মৌল কোনটি?",
                optionA = "অক্সিজেন",
                optionB = "ফ্লোরিন",
                optionC = "ক্লোরিন",
                optionD = "নাইট্রোজেন",
                correctOption = "B",
                explanationBn = "ফ্লোরিনের (F) তড়িৎ ঋণাত্মকতা সর্বোচ্চ (৪.০)।",
                isAiGenerated = true,
                sourceInfo = "AI Practice"
            )
        )

        val activeState = ActiveTestState(
            sessionKey = sessionKey,
            titleEn = "Medical Practice Test",
            titleBn = "মেডিকেল অনুশীলন পরীক্ষা",
            mode = TestMode.MEDICAL,
            universityId = "medical_mbbs",
            unitId = "mbbs_official",
            sessionYear = "2024-25",
            sourceType = QuestionSourceType.AI_PRACTICE,
            questions = sampleQuestions,
            selectedAnswers = mapOf(0 to "C", 1 to "A"),
            totalDurationSeconds = 120,
            remainingSeconds = 90,
            positiveMark = 1.0,
            negativeMark = 0.25,
            unansweredMark = 0.0,
            isOfficialPattern = true
        )

        val evaluated = ScoringEngine.evaluateTest(uid, activeState)
        val recordId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveTestResult(evaluated).getOrThrow()
        }
        assertTrue(recordId.isNotBlank())

        val history = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeTestHistory(uid).first { list -> list.any { it.id == recordId } }
        }
        val savedRecord = history.first { it.id == recordId }
        assertEquals(0.75, savedRecord.totalScore, 0.001)
        assertEquals(1L, savedRecord.correctCount)
        assertEquals(1L, savedRecord.wrongCount)

        val reviewItems = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getTestReviewItems(uid, recordId).getOrThrow()
        }
        assertEquals(2, reviewItems.size)
        assertEquals("C", reviewItems[0].selectedOption)
    }

    @Test
    fun observeTestHistory_unauthenticatedCaller_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = MockTestRepository(firestore)

        var caught: Throwable? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeTestHistory("any_uid").first()
            }
        } catch (e: Throwable) {
            caught = e
        }
        assertNotNull(caught)
        val firestoreException = checkNotNull(caught?.unwrapFirestoreException()) {
            "Expected nested FirebaseFirestoreException, got $caught"
        }
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
    }

    @Test
    fun getTestReviewItems_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = MockTestRepository(firestore)

        val evaluated = ScoringEngine.evaluateTest(
            userId = aliceUid,
            session = ActiveTestState(
                sessionKey = "s_${UUID.randomUUID()}",
                titleEn = "DU A Unit",
                titleBn = "ঢাকা বিশ্ববিদ্যালয় ক ইউনিট",
                mode = TestMode.VARSITY_MODEL_TEST,
                universityId = "du",
                unitId = "du_a",
                sessionYear = "2024-25",
                sourceType = QuestionSourceType.AI_PRACTICE,
                questions = listOf(
                    MockQuestion(
                        id = "q1",
                        index = 0,
                        subject = "Physics",
                        topic = "ভেক্টর",
                        questionBn = "দুটি সমান ভেক্টরের লব্ধি একটির সমান হলে মধ্যবর্তী কোণ কত?",
                        optionA = "৬০°",
                        optionB = "৯০°",
                        optionC = "১২০°",
                        optionD = "১৮০°",
                        correctOption = "C",
                        explanationBn = "P = Q = R হলে cos θ = -1/2 অর্থাৎ θ = ১২০°।",
                        isAiGenerated = true,
                        sourceInfo = "AI Practice"
                    )
                ),
                selectedAnswers = mapOf(0 to "C"),
                totalDurationSeconds = 300,
                remainingSeconds = 200,
                positiveMark = 1.0,
                negativeMark = 0.25,
                unansweredMark = 0.0,
                isOfficialPattern = true
            )
        )
        val recordId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.saveTestResult(evaluated).getOrThrow()
        }

        // Bob tries to read Alice's test review items
        signInTestUser(BOB_EMAIL)
        val bobRepo = MockTestRepository(firestore)
        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getTestReviewItems(aliceUid, recordId)
        }
        assertTrue(result.isFailure)
        val firestoreEx = checkNotNull(result.exceptionOrNull()?.unwrapFirestoreException())
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx.code)
    }

    @Test
    fun upsertUniversity_nonAdminUser_failsWithPermissionDenied() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = MockTestRepository(firestore)
        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.upsertUniversity(AdmissionCatalogDefaults.universities.first())
        }
        assertTrue(result.isFailure)
        val firestoreEx = checkNotNull(result.exceptionOrNull()?.unwrapFirestoreException())
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx.code)
    }

    @Test
    fun upsertUserProfile_authenticatedUser_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = MockTestRepository(firestore)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.upsertUserProfile(uid, "Alice Student", AppLanguage.BN)
        }
        val doc = withTimeout(DEFAULT_TIMEOUT_MS) {
            firestore.collection("users").document(uid).get().await()
        }
        assertTrue(doc.exists())
        assertEquals("Alice Student", doc.getString("displayName"))
    }

    private companion object {
        const val ALICE_EMAIL = "alice_mock@test.com"
        const val BOB_EMAIL = "bob_mock@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
