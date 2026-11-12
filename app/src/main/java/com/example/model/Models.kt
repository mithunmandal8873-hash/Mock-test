package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class AppLanguage(val code: String, val label: String) {
    BN("bn", "বাংলা"),
    EN("en", "English");

    companion object {
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code == code } ?: EN
    }
}

enum class UniversityCategory(val key: String) {
    PUBLIC("PUBLIC"),
    PRIVATE("PRIVATE"),
    SCIENCE_TECH("SCIENCE_TECH"),
    AGRICULTURAL("AGRICULTURAL"),
    ENGINEERING("ENGINEERING"),
    CLUSTER("CLUSTER");

    companion object {
        fun fromKey(key: String?): UniversityCategory =
            entries.firstOrNull { it.key == key } ?: PUBLIC
    }
}

enum class QuestionDifficulty(val key: String) {
    EASY("EASY"),
    MODERATE("MODERATE"),
    HARD("HARD"),
    ADMISSION_STANDARD("ADMISSION_STANDARD"),
    MIXED("MIXED");

    companion object {
        fun fromKey(key: String?): QuestionDifficulty =
            entries.firstOrNull { it.key == key } ?: ADMISSION_STANDARD
    }
}

enum class TestMode(val key: String) {
    VARSITY_QUESTION_BANK("VARSITY_QUESTION_BANK"),
    VARSITY_MODEL_TEST("VARSITY_MODEL_TEST"),
    MEDICAL("MEDICAL");

    companion object {
        fun fromKey(key: String?): TestMode =
            entries.firstOrNull { it.key == key } ?: VARSITY_QUESTION_BANK
    }
}

enum class QuestionSourceType(val key: String) {
    VERIFIED_BANK("VERIFIED_BANK"),
    AI_PRACTICE("AI_PRACTICE");

    companion object {
        fun fromKey(key: String?): QuestionSourceType =
            entries.firstOrNull { it.key == key } ?: AI_PRACTICE
    }
}

data class SubjectSpec(
    val id: String = "",
    val nameEn: String = "",
    val nameBn: String = "",
    val defaultCount: Int = 15,
    val isCompulsory: Boolean = true
)

data class AdmissionUnit(
    val id: String = "",
    val nameEn: String = "",
    val nameBn: String = "",
    val patternVerified: Boolean = false,
    val sessionYear: String = "2024-25",
    val totalQuestions: Int = 0,
    val durationMinutes: Int = 60,
    val positiveMark: Double = 1.0,
    val negativeMark: Double = 0.25,
    val unansweredMark: Double = 0.0,
    val totalMarks: Double = 0.0,
    val subjects: List<SubjectSpec> = emptyList(),
    val sourceReference: String = "",
    val verificationDate: String = "",
    val notesEn: String = "",
    val notesBn: String = ""
)

@IgnoreExtraProperties
data class UniversityConfig(
    @DocumentId val id: String = "",
    val nameEn: String = "",
    val nameBn: String = "",
    val abbreviation: String = "",
    val category: String = UniversityCategory.PUBLIC.key,
    val sessionYear: String = "2024-25",
    val sourceReference: String = "",
    val verificationDate: String = "",
    val isPatternVerified: Boolean = false,
    val units: List<AdmissionUnit> = emptyList(),
    val visibility: String = "public",
    val updatedBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun categoryEnum(): UniversityCategory = UniversityCategory.fromKey(category)
}

@IgnoreExtraProperties
data class VerifiedQuestion(
    @DocumentId val id: String = "",
    val universityId: String = "",
    val unitId: String = "",
    val examYear: String = "",
    val sourceLabel: String = "",
    val subject: String = "",
    val topic: String = "",
    val difficulty: String = QuestionDifficulty.ADMISSION_STANDARD.key,
    val questionBn: String = "",
    val optionA: String = "",
    val optionB: String = "",
    val optionC: String = "",
    val optionD: String = "",
    val correctOption: String = "A",
    val explanationBn: String = "",
    val visibility: String = "public",
    val verifiedBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class MockQuestion(
    val id: String = "",
    val index: Int = 0,
    val subject: String = "",
    val topic: String = "",
    val questionBn: String = "",
    val optionA: String = "",
    val optionB: String = "",
    val optionC: String = "",
    val optionD: String = "",
    val correctOption: String = "A",
    val explanationBn: String = "",
    val isAiGenerated: Boolean = true,
    val sourceInfo: String = ""
)

data class TestConfig(
    val mode: TestMode,
    val titleEn: String,
    val titleBn: String,
    val universityId: String,
    val unitId: String,
    val sessionYear: String,
    val sourceType: QuestionSourceType,
    val difficulty: QuestionDifficulty,
    val subjectCounts: Map<String, Int>,
    val totalQuestions: Int,
    val durationMinutes: Int,
    val positiveMark: Double,
    val negativeMark: Double,
    val unansweredMark: Double,
    val isOfficialPattern: Boolean,
    val patternReference: String = ""
)

data class ActiveTestState(
    val sessionKey: String,
    val titleEn: String,
    val titleBn: String,
    val mode: TestMode,
    val universityId: String,
    val unitId: String,
    val sessionYear: String,
    val sourceType: QuestionSourceType,
    val questions: List<MockQuestion>,
    val selectedAnswers: Map<Int, String>,
    val totalDurationSeconds: Int,
    val remainingSeconds: Int,
    val positiveMark: Double,
    val negativeMark: Double,
    val unansweredMark: Double,
    val isOfficialPattern: Boolean
)

@IgnoreExtraProperties
data class TestRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val mode: String = TestMode.VARSITY_QUESTION_BANK.key,
    val titleEn: String = "",
    val titleBn: String = "",
    val universityId: String = "",
    val unitId: String = "",
    val sessionYear: String = "",
    val sourceType: String = QuestionSourceType.AI_PRACTICE.key,
    val totalQuestions: Long = 0L,
    val attemptedCount: Long = 0L,
    val correctCount: Long = 0L,
    val wrongCount: Long = 0L,
    val unansweredCount: Long = 0L,
    val totalScore: Double = 0.0,
    val maxPossibleScore: Double = 0.0,
    val accuracyPercent: Double = 0.0,
    val durationSeconds: Long = 0L,
    val timeTakenSeconds: Long = 0L,
    val positiveMark: Double = 1.0,
    val negativeMark: Double = 0.25,
    val unansweredMark: Double = 0.0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class TestReviewItem(
    @DocumentId val id: String = "",
    val questionIndex: Long = 0L,
    val subject: String = "",
    val topic: String = "",
    val questionBn: String = "",
    val optionA: String = "",
    val optionB: String = "",
    val optionC: String = "",
    val optionD: String = "",
    val correctOption: String = "A",
    val selectedOption: String = "",
    val explanationBn: String = "",
    val isAiGenerated: Boolean = true,
    val sourceInfo: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class SubmittedTestResult(
    val record: TestRecord,
    val items: List<TestReviewItem>
)

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val preferredLanguage: String = "en",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
