package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AdmissionCatalogDefaults
import com.example.data.MockTestRepository
import com.example.data.QuestionGenerator
import com.example.data.ScoringEngine
import com.example.model.ActiveTestState
import com.example.model.AdmissionUnit
import com.example.model.AppLanguage
import com.example.model.MockQuestion
import com.example.model.QuestionSourceType
import com.example.model.SubmittedTestResult
import com.example.model.TestConfig
import com.example.model.TestMode
import com.example.model.TestRecord
import com.example.model.UniversityCategory
import com.example.model.UniversityConfig
import com.example.model.VerifiedQuestion
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface GenerationUiState {
    data object Idle : GenerationUiState
    data object Loading : GenerationUiState
    data class VerifiedBankInsufficient(
        val config: TestConfig,
        val availableCount: Int,
        val requestedCount: Int
    ) : GenerationUiState
    data class Error(val config: TestConfig, val message: String) : GenerationUiState
}

sealed interface AdminActionState {
    data object Idle : AdminActionState
    data object Saving : AdminActionState
    data class Success(val message: String) : AdminActionState
    data class Error(val message: String) : AdminActionState
}

class MockTestViewModel(
    private val repository: MockTestRepository,
    private val questionGenerator: QuestionGenerator,
    private val currentUserId: String,
    private val userDisplayName: String
) : ViewModel() {

    val language: StateFlow<AppLanguage> = repository.observeLanguage()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = AppLanguage.EN
        )

    val universities: StateFlow<List<UniversityConfig>> = repository.observeUniversities()
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            Log.w(TAG, "Error observing universities", firestoreError ?: error)
            emit(AdmissionCatalogDefaults.universities)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = AdmissionCatalogDefaults.universities
        )

    val verifiedQuestions: StateFlow<List<VerifiedQuestion>> = repository.observeVerifiedQuestions()
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            Log.w(TAG, "Error observing verified questions", firestoreError ?: error)
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = emptyList()
        )

    val testHistory: StateFlow<List<TestRecord>> = repository.observeTestHistory(currentUserId)
        .map { it }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            Log.w(TAG, "Error observing test history", firestoreError ?: error)
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = emptyList()
        )

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<UniversityCategory?>(null)
    val selectedCategory: StateFlow<UniversityCategory?> = _selectedCategory.asStateFlow()

    private val _selectedUniversityId = MutableStateFlow<String?>(null)
    val selectedUniversityId: StateFlow<String?> = _selectedUniversityId.asStateFlow()

    private val _selectedUnitId = MutableStateFlow<String?>(null)
    val selectedUnitId: StateFlow<String?> = _selectedUnitId.asStateFlow()

    private val _generationState = MutableStateFlow<GenerationUiState>(GenerationUiState.Idle)
    val generationState: StateFlow<GenerationUiState> = _generationState.asStateFlow()

    private val _activeTest = MutableStateFlow<ActiveTestState?>(null)
    val activeTest: StateFlow<ActiveTestState?> = _activeTest.asStateFlow()

    private val _savedActiveSession = MutableStateFlow<ActiveTestState?>(null)
    val savedActiveSession: StateFlow<ActiveTestState?> = _savedActiveSession.asStateFlow()

    private val _submittedResult = MutableStateFlow<SubmittedTestResult?>(null)
    val submittedResult: StateFlow<SubmittedTestResult?> = _submittedResult.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _autoSubmitNotice = MutableStateFlow(false)
    val autoSubmitNotice: StateFlow<Boolean> = _autoSubmitNotice.asStateFlow()

    private val _adminActionState = MutableStateFlow<AdminActionState>(AdminActionState.Idle)
    val adminActionState: StateFlow<AdminActionState> = _adminActionState.asStateFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            repository.upsertUserProfile(currentUserId, userDisplayName, language.value)
            _isAdmin.value = repository.checkIsAdmin(currentUserId)
            _savedActiveSession.value = repository.loadLocalActiveSession()
        }
    }

    fun setLanguage(lang: AppLanguage) {
        viewModelScope.launch {
            repository.saveLanguage(lang, currentUserId, userDisplayName)
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: UniversityCategory?) {
        _selectedCategory.value = category
    }

    fun selectUniversity(universityId: String) {
        _selectedUniversityId.value = universityId
        _selectedUnitId.value = null
    }

    fun selectUnit(unitId: String) {
        _selectedUnitId.value = unitId
    }

    fun clearGenerationState() {
        _generationState.value = GenerationUiState.Idle
    }

    fun startTest(config: TestConfig, onReadyToRun: () -> Unit) {
        viewModelScope.launch {
            _generationState.value = GenerationUiState.Loading
            try {
                val questions: List<MockQuestion> = when (config.sourceType) {
                    QuestionSourceType.VERIFIED_BANK -> {
                        val fetched = repository.fetchVerifiedQuestionsForTest(
                            universityId = config.universityId,
                            unitId = config.unitId,
                            subjectCounts = config.subjectCounts
                        )
                        if (fetched.size < config.totalQuestions || fetched.isEmpty()) {
                            _generationState.value = GenerationUiState.VerifiedBankInsufficient(
                                config = config,
                                availableCount = fetched.size,
                                requestedCount = config.totalQuestions
                            )
                            return@launch
                        }
                        fetched
                    }
                    QuestionSourceType.AI_PRACTICE -> {
                        questionGenerator.generateQuestions(config)
                    }
                }

                val durationSec = (config.durationMinutes * 60).coerceAtLeast(60)
                val newSession = ActiveTestState(
                    sessionKey = UUID.randomUUID().toString(),
                    titleEn = config.titleEn,
                    titleBn = config.titleBn,
                    mode = config.mode,
                    universityId = config.universityId,
                    unitId = config.unitId,
                    sessionYear = config.sessionYear,
                    sourceType = config.sourceType,
                    questions = questions,
                    selectedAnswers = emptyMap(),
                    totalDurationSeconds = durationSec,
                    remainingSeconds = durationSec,
                    positiveMark = config.positiveMark,
                    negativeMark = config.negativeMark,
                    unansweredMark = config.unansweredMark,
                    isOfficialPattern = config.isOfficialPattern
                )
                _activeTest.value = newSession
                _savedActiveSession.value = newSession
                _autoSubmitNotice.value = false
                _generationState.value = GenerationUiState.Idle
                repository.saveActiveSession(newSession)
                startCountdownTimer(onAutoSubmitted = {})
                onReadyToRun()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start test", e)
                _generationState.value = GenerationUiState.Error(
                    config = config,
                    message = e.localizedMessage ?: "Failed to generate questions. Please retry."
                )
            }
        }
    }

    fun resumeSavedSession(onResumed: () -> Unit) {
        val saved = _savedActiveSession.value ?: return
        _activeTest.value = saved
        _autoSubmitNotice.value = false
        startCountdownTimer(onAutoSubmitted = {})
        onResumed()
    }

    fun discardSavedSession() {
        timerJob?.cancel()
        _activeTest.value = null
        _savedActiveSession.value = null
        viewModelScope.launch {
            repository.clearActiveSession(currentUserId)
        }
    }

    fun selectAnswer(questionIndex: Int, option: String) {
        val current = _activeTest.value ?: return
        val updatedAnswers = current.selectedAnswers.toMutableMap().apply {
            this[questionIndex] = option
        }
        val updatedState = current.copy(selectedAnswers = updatedAnswers)
        _activeTest.value = updatedState
        _savedActiveSession.value = updatedState
        viewModelScope.launch {
            repository.saveActiveSession(updatedState)
        }
    }

    fun ensureTimerRunning(onAutoSubmitted: () -> Unit) {
        if (timerJob?.isActive != true && _activeTest.value != null) {
            startCountdownTimer(onAutoSubmitted)
        }
    }

    private fun startCountdownTimer(onAutoSubmitted: () -> Unit) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                val current = _activeTest.value ?: break
                if (current.remainingSeconds <= 0) {
                    _autoSubmitNotice.value = true
                    submitActiveTestInternal(onSubmitted = onAutoSubmitted)
                    break
                }
                delay(1000L)
                val latest = _activeTest.value ?: break
                val nextRemaining = (latest.remainingSeconds - 1).coerceAtLeast(0)
                val updated = latest.copy(remainingSeconds = nextRemaining)
                _activeTest.value = updated
                _savedActiveSession.value = updated
                if (nextRemaining % 10 == 0 || nextRemaining == 0) {
                    repository.saveActiveSession(updated)
                }
                if (nextRemaining == 0) {
                    _autoSubmitNotice.value = true
                    submitActiveTestInternal(onSubmitted = onAutoSubmitted)
                    break
                }
            }
        }
    }

    fun submitActiveTest(onSubmitted: () -> Unit) {
        viewModelScope.launch {
            submitActiveTestInternal(onSubmitted)
        }
    }

    private suspend fun submitActiveTestInternal(onSubmitted: () -> Unit) {
        if (_isSubmitting.value) return
        val session = _activeTest.value ?: return
        _isSubmitting.value = true
        timerJob?.cancel()
        try {
            val evaluated = ScoringEngine.evaluateTest(currentUserId, session)
            _submittedResult.value = evaluated
            repository.saveTestResult(evaluated)
            _activeTest.value = null
            _savedActiveSession.value = null
            onSubmitted()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error submitting test", e)
            // Keep evaluated result visible even if offline save retries via Firestore cache
            onSubmitted()
        } finally {
            _isSubmitting.value = false
        }
    }

    fun openHistoryRecord(record: TestRecord, onLoaded: () -> Unit) {
        viewModelScope.launch {
            val result = repository.getTestReviewItems(currentUserId, record.id)
            val items = result.getOrNull().orEmpty()
            _submittedResult.value = SubmittedTestResult(record = record, items = items)
            onLoaded()
        }
    }

    // --- Admin Actions ---
    fun clearAdminStatus() {
        _adminActionState.value = AdminActionState.Idle
    }

    fun seedDefaultsToCloud() {
        viewModelScope.launch {
            _adminActionState.value = AdminActionState.Saving
            val res = repository.seedDefaultUniversitiesToCloud()
            _adminActionState.value = res.fold(
                onSuccess = { AdminActionState.Success("Default catalogue synced to cloud") },
                onFailure = { AdminActionState.Error(it.localizedMessage ?: "Sync failed") }
            )
        }
    }

    fun saveUniversity(university: UniversityConfig) {
        viewModelScope.launch {
            _adminActionState.value = AdminActionState.Saving
            val res = repository.upsertUniversity(university)
            _adminActionState.value = res.fold(
                onSuccess = { AdminActionState.Success("Saved ${university.abbreviation}") },
                onFailure = { AdminActionState.Error(it.localizedMessage ?: "Save failed") }
            )
        }
    }

    fun deleteUniversity(universityId: String) {
        viewModelScope.launch {
            _adminActionState.value = AdminActionState.Saving
            val res = repository.deleteUniversity(universityId)
            _adminActionState.value = res.fold(
                onSuccess = { AdminActionState.Success("Deleted university") },
                onFailure = { AdminActionState.Error(it.localizedMessage ?: "Delete failed") }
            )
        }
    }

    fun saveVerifiedQuestion(question: VerifiedQuestion) {
        viewModelScope.launch {
            _adminActionState.value = AdminActionState.Saving
            val res = repository.upsertVerifiedQuestion(question)
            _adminActionState.value = res.fold(
                onSuccess = { AdminActionState.Success("Verified question saved") },
                onFailure = { AdminActionState.Error(it.localizedMessage ?: "Save failed") }
            )
        }
    }

    fun deleteVerifiedQuestion(questionId: String) {
        viewModelScope.launch {
            _adminActionState.value = AdminActionState.Saving
            val res = repository.deleteVerifiedQuestion(questionId)
            _adminActionState.value = res.fold(
                onSuccess = { AdminActionState.Success("Question deleted") },
                onFailure = { AdminActionState.Error(it.localizedMessage ?: "Delete failed") }
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    private companion object {
        const val TAG = "MockTestViewModel"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
