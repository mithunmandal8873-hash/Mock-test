package com.example.data

import com.example.model.ActiveTestState
import com.example.model.MockQuestion
import com.example.model.QuestionSourceType
import com.example.model.TestMode
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ScoringAndValidationUnitTest {

    @Test
    fun evaluateTest_calculatesPositiveNegativeUnansweredAndAccuracyCorrectly() {
        val questions = listOf(
            MockQuestion("1", 0, "Biology", "Cell", "Q1?", "A", "B", "C", "D", "A", "Exp1", true, "AI"),
            MockQuestion("2", 1, "Biology", "Cell", "Q2?", "A", "B", "C", "D", "B", "Exp2", true, "AI"),
            MockQuestion("3", 2, "Chemistry", "Atom", "Q3?", "A", "B", "C", "D", "C", "Exp3", true, "AI"),
            MockQuestion("4", 3, "Physics", "Force", "Q4?", "A", "B", "C", "D", "D", "Exp4", true, "AI")
        )
        val session = ActiveTestState(
            sessionKey = "test_1",
            titleEn = "Medical Test",
            titleBn = "মেডিকেল টেস্ট",
            mode = TestMode.MEDICAL,
            universityId = "medical_mbbs",
            unitId = "mbbs_official",
            sessionYear = "2024-25",
            sourceType = QuestionSourceType.AI_PRACTICE,
            questions = questions,
            selectedAnswers = mapOf(
                0 to "A", // Correct (+1.0)
                1 to "C", // Wrong (-0.25)
                2 to "C"  // Correct (+1.0)
                // index 3 unanswered (0.0)
            ),
            totalDurationSeconds = 3600,
            remainingSeconds = 3000,
            positiveMark = 1.0,
            negativeMark = 0.25,
            unansweredMark = 0.0,
            isOfficialPattern = true
        )

        val result = ScoringEngine.evaluateTest("u1", session)
        assertEquals(4L, result.record.totalQuestions)
        assertEquals(3L, result.record.attemptedCount)
        assertEquals(2L, result.record.correctCount)
        assertEquals(1L, result.record.wrongCount)
        assertEquals(1L, result.record.unansweredCount)
        assertEquals(1.75, result.record.totalScore, 0.001)
        assertEquals(66.666, result.record.accuracyPercent, 0.01)
        assertEquals(600L, result.record.timeTakenSeconds)
    }

    @Test
    fun parseAndValidateQuestionsJson_filtersDuplicatesAndInvalidEntries() {
        val json = """
            {
              "questions": [
                {
                  "subject": "Biology",
                  "topic": "কোষ",
                  "questionBn": "মাইটোকন্ড্রিয়ার প্রধান কাজ কী?",
                  "optionA": "শক্তি উৎপাদন",
                  "optionB": "প্রোটিন সংশ্লেষ",
                  "optionC": "লিপিড সঞ্চয়",
                  "optionD": "ডিএনএ প্রতিলিপি",
                  "correctOption": "A",
                  "explanationBn": "মাইটোকন্ড্রিয়া কোষের পাওয়ার হাউস হিসেবে ATP উৎপাদন করে।"
                },
                {
                  "subject": "Biology",
                  "topic": "কোষ",
                  "questionBn": "মাইটোকন্ড্রিয়ার প্রধান কাজ কী?",
                  "optionA": "শক্তি উৎপাদন",
                  "optionB": "প্রোটিন সংশ্লেষ",
                  "optionC": "লিপিড সঞ্চয়",
                  "optionD": "ডিএনএ প্রতিলিপি",
                  "correctOption": "A",
                  "explanationBn": "ডুপ্লিকেট প্রশ্ন বাদ পড়বে।"
                },
                {
                  "subject": "Chemistry",
                  "topic": "অম্ল-ক্ষার",
                  "questionBn": "পানির pH মান কত?",
                  "optionA": "৭",
                  "optionB": "৭",
                  "optionC": "৫",
                  "optionD": "৯",
                  "correctOption": "A",
                  "explanationBn": "ডুপ্লিকেট অপশন থাকায় বাদ পড়বে।"
                }
              ]
            }
        """.trimIndent()

        val parsed = FirebaseAiQuestionGenerator.parseAndValidateQuestionsJson(json)
        assertEquals(1, parsed.size)
        assertEquals("A", parsed[0].correctOption)
    }
}
