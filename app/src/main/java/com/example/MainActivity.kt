package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.auth.signOut
import com.example.data.AdmissionCatalogDefaults
import com.example.data.FirebaseAiQuestionGenerator
import com.example.data.MockTestRepository
import com.example.data.mockTestDataStore
import com.example.firebase.configureAppCheck
import com.example.model.AppLanguage
import com.example.model.TestMode
import com.example.ui.MockTestViewModel
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.EntryScreen
import com.example.ui.screens.MedicalConfigScreen
import com.example.ui.screens.ResultReviewScreen
import com.example.ui.screens.TestRunnerScreen
import com.example.ui.screens.UnitSelectionScreen
import com.example.ui.screens.VarsityCatalogueScreen
import com.example.ui.screens.VarsityConfigScreen
import com.example.ui.theme.MockTestTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

private object Routes {
    const val ENTRY = "entry"
    const val VARSITY_CATALOGUE = "varsity_catalogue"
    const val UNIT_SELECTION = "unit_selection"
    const val VARSITY_CONFIG = "varsity_config/{mode}"
    const val MEDICAL_CONFIG = "medical_config"
    const val TEST_RUNNER = "test_runner"
    const val RESULT_REVIEW = "result_review"
    const val ADMIN_PANEL = "admin_panel"

    fun varsityConfig(mode: TestMode) = "varsity_config/${mode.key}"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Mandatory App Check configuration with launch Intent before any UI or ViewModels
        configureAppCheck(this, intent)

        enableEdgeToEdge()
        setContent {
            MockTestTheme {
                AppRoot()
            }
        }
    }
}

@Composable
private fun AppRoot() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
    var authScreenLanguage by remember { mutableStateOf(AppLanguage.EN) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    val user = currentUser
    if (user == null) {
        AuthScreen(
            language = authScreenLanguage,
            onToggleLanguage = { authScreenLanguage = it },
            onAuthSuccess = { currentUser = Firebase.auth.currentUser }
        )
    } else {
        val uid = user.uid
        val displayName = user.displayName ?: user.email ?: "Student"
        key(uid) {
            val viewModel: MockTestViewModel = viewModel(
                key = uid,
                factory = viewModelFactory {
                    initializer {
                        val app = checkNotNull(this[APPLICATION_KEY])
                        val databaseId = app.getString(R.string.firestore_database_id)
                        val db = FirebaseFirestore.getInstance(databaseId)
                        val repo = MockTestRepository(db, app.mockTestDataStore)
                        MockTestViewModel(
                            repository = repo,
                            questionGenerator = FirebaseAiQuestionGenerator(),
                            currentUserId = uid,
                            userDisplayName = displayName
                        )
                    }
                }
            )
            MockTestNavGraph(
                viewModel = viewModel,
                onSignOut = {
                    signOut(credentialManager = credentialManager, scope = scope)
                }
            )
        }
    }
}

@Composable
private fun MockTestNavGraph(
    viewModel: MockTestViewModel,
    onSignOut: () -> Unit
) {
    val navController = rememberNavController()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val universities by viewModel.universities.collectAsStateWithLifecycle()
    val verifiedQuestions by viewModel.verifiedQuestions.collectAsStateWithLifecycle()
    val testHistory by viewModel.testHistory.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val selectedUniversityId by viewModel.selectedUniversityId.collectAsStateWithLifecycle()
    val selectedUnitId by viewModel.selectedUnitId.collectAsStateWithLifecycle()
    val generationState by viewModel.generationState.collectAsStateWithLifecycle()
    val activeTest by viewModel.activeTest.collectAsStateWithLifecycle()
    val savedActiveSession by viewModel.savedActiveSession.collectAsStateWithLifecycle()
    val submittedResult by viewModel.submittedResult.collectAsStateWithLifecycle()
    val isSubmitting by viewModel.isSubmitting.collectAsStateWithLifecycle()
    val autoSubmitNotice by viewModel.autoSubmitNotice.collectAsStateWithLifecycle()
    val adminActionState by viewModel.adminActionState.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = Routes.ENTRY
    ) {
        composable(Routes.ENTRY) {
            EntryScreen(
                language = language,
                onToggleLanguage = viewModel::setLanguage,
                savedSession = savedActiveSession,
                recentHistory = testHistory,
                onResumeSession = {
                    viewModel.resumeSavedSession {
                        navController.navigate(Routes.TEST_RUNNER)
                    }
                },
                onDiscardSession = viewModel::discardSavedSession,
                onSelectVarsity = { navController.navigate(Routes.VARSITY_CATALOGUE) },
                onSelectMedical = {
                    viewModel.clearGenerationState()
                    navController.navigate(Routes.MEDICAL_CONFIG)
                },
                onOpenRecord = { record ->
                    viewModel.openHistoryRecord(record) {
                        navController.navigate(Routes.RESULT_REVIEW)
                    }
                },
                onOpenAdmin = {
                    viewModel.clearAdminStatus()
                    navController.navigate(Routes.ADMIN_PANEL)
                },
                onSignOut = onSignOut
            )
        }

        composable(Routes.VARSITY_CATALOGUE) {
            VarsityCatalogueScreen(
                language = language,
                onToggleLanguage = viewModel::setLanguage,
                universities = universities,
                searchQuery = searchQuery,
                onSearchChange = viewModel::updateSearchQuery,
                selectedCategory = selectedCategory,
                onSelectCategory = viewModel::selectCategory,
                onSelectUniversity = { uni ->
                    viewModel.selectUniversity(uni.id)
                    navController.navigate(Routes.UNIT_SELECTION)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.UNIT_SELECTION) {
            val selectedUni = universities.firstOrNull { it.id == selectedUniversityId }
                ?: AdmissionCatalogDefaults.universities.first()
            UnitSelectionScreen(
                language = language,
                onToggleLanguage = viewModel::setLanguage,
                university = selectedUni,
                selectedUnitId = selectedUnitId,
                onSelectUnit = { unit -> viewModel.selectUnit(unit.id) },
                onProceedMode = { unit, mode ->
                    viewModel.selectUnit(unit.id)
                    viewModel.clearGenerationState()
                    navController.navigate(Routes.varsityConfig(mode))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.VARSITY_CONFIG) { backStackEntry ->
            val modeArg = backStackEntry.arguments?.getString("mode")
            val mode = TestMode.fromKey(modeArg)
            val selectedUni = universities.firstOrNull { it.id == selectedUniversityId }
                ?: AdmissionCatalogDefaults.universities.first()
            val selectedUnit = selectedUni.units.firstOrNull { it.id == selectedUnitId }
                ?: selectedUni.units.first()

            VarsityConfigScreen(
                language = language,
                onToggleLanguage = viewModel::setLanguage,
                university = selectedUni,
                unit = selectedUnit,
                initialMode = mode,
                generationState = generationState,
                onSwitchToQuestionBank = {
                    navController.navigate(Routes.varsityConfig(TestMode.VARSITY_QUESTION_BANK)) {
                        popUpTo(Routes.UNIT_SELECTION)
                    }
                },
                onStartTest = { config ->
                    viewModel.startTest(config) {
                        navController.navigate(Routes.TEST_RUNNER) {
                            popUpTo(Routes.ENTRY)
                        }
                    }
                },
                onClearError = viewModel::clearGenerationState,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.MEDICAL_CONFIG) {
            MedicalConfigScreen(
                language = language,
                onToggleLanguage = viewModel::setLanguage,
                generationState = generationState,
                onStartTest = { config ->
                    viewModel.startTest(config) {
                        navController.navigate(Routes.TEST_RUNNER) {
                            popUpTo(Routes.ENTRY)
                        }
                    }
                },
                onClearError = viewModel::clearGenerationState,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.TEST_RUNNER) {
            val session = activeTest
            if (session != null) {
                TestRunnerScreen(
                    language = language,
                    onToggleLanguage = viewModel::setLanguage,
                    activeTest = session,
                    isSubmitting = isSubmitting,
                    autoSubmitNotice = autoSubmitNotice,
                    onSelectAnswer = viewModel::selectAnswer,
                    onConfirmSubmit = {
                        viewModel.submitActiveTest {
                            navController.navigate(Routes.RESULT_REVIEW) {
                                popUpTo(Routes.ENTRY)
                            }
                        }
                    },
                    onEnsureTimerRunning = {
                        viewModel.ensureTimerRunning {
                            navController.navigate(Routes.RESULT_REVIEW) {
                                popUpTo(Routes.ENTRY)
                            }
                        }
                    },
                    onExitTest = {
                        navController.navigate(Routes.ENTRY) {
                            popUpTo(Routes.ENTRY) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(Routes.RESULT_REVIEW) {
            val result = submittedResult
            if (result != null) {
                ResultReviewScreen(
                    language = language,
                    onToggleLanguage = viewModel::setLanguage,
                    result = result,
                    onBackToHome = {
                        navController.navigate(Routes.ENTRY) {
                            popUpTo(Routes.ENTRY) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(Routes.ADMIN_PANEL) {
            AdminPanelScreen(
                language = language,
                onToggleLanguage = viewModel::setLanguage,
                isAdmin = isAdmin,
                universities = universities,
                verifiedQuestions = verifiedQuestions,
                adminActionState = adminActionState,
                onSeedDefaults = viewModel::seedDefaultsToCloud,
                onSaveUniversity = viewModel::saveUniversity,
                onDeleteUniversity = viewModel::deleteUniversity,
                onSaveQuestion = viewModel::saveVerifiedQuestion,
                onDeleteQuestion = viewModel::deleteVerifiedQuestion,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
