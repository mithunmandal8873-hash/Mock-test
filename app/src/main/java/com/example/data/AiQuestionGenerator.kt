package com.example.data

import android.util.Log
import com.example.model.MockQuestion
import com.example.model.QuestionDifficulty
import com.example.model.TestConfig
import com.example.model.TestMode
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "AiQuestionGenerator"
private const val CHUNK_SIZE = 20

interface QuestionGenerator {
    suspend fun generateQuestions(config: TestConfig): List<MockQuestion>
}

class FirebaseAiQuestionGenerator : QuestionGenerator {

    private val questionSchema = Schema.obj(
        properties = mapOf(
            "questions" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "subject" to Schema.string(description = "Subject name"),
                        "topic" to Schema.string(description = "Specific syllabus topic in Bangla"),
                        "questionBn" to Schema.string(description = "MCQ stem written in natural Bangla"),
                        "optionA" to Schema.string(description = "Option A text in Bangla"),
                        "optionB" to Schema.string(description = "Option B text in Bangla"),
                        "optionC" to Schema.string(description = "Option C text in Bangla"),
                        "optionD" to Schema.string(description = "Option D text in Bangla"),
                        "correctOption" to Schema.enumeration(
                            values = listOf("A", "B", "C", "D"),
                            description = "Single correct option letter"
                        ),
                        "explanationBn" to Schema.string(description = "Detailed step-by-step explanation in Bangla")
                    )
                ),
                description = "List of unique, validated Bangladesh admission MCQs"
            )
        )
    )

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = questionSchema
                temperature = 0.65f
            }
        )
    }

    override suspend fun generateQuestions(config: TestConfig): List<MockQuestion> {
        val activeSubjects = config.subjectCounts.filterValues { it > 0 }
        require(activeSubjects.isNotEmpty()) { "At least one subject must have > 0 questions." }

        val generated = mutableListOf<MockQuestion>()
        val seenStems = mutableSetOf<String>()

        // Chunk by subject or batch up to CHUNK_SIZE questions per request to prevent JSON truncation
        val flatPlan = buildChunkPlan(activeSubjects)
        for (chunkMap in flatPlan) {
            val chunkCount = chunkMap.values.sum()
            if (chunkCount <= 0) continue

            val prompt = buildBanglaPrompt(config, chunkMap, seenStems.toList().takeLast(15))
            val parsedChunk = requestAndValidateChunk(
                prompt = prompt,
                expectedCount = chunkCount,
                config = config,
                seenStems = seenStems
            )
            generated.addAll(parsedChunk)
        }

        val reindexed = generated.mapIndexed { idx, q ->
            q.copy(id = "ai_${idx}_${q.subject.hashCode()}", index = idx)
        }

        if (reindexed.isEmpty()) {
            throw IllegalStateException("AI could not produce valid Bangla questions. Please retry.")
        }
        return reindexed
    }

    private suspend fun requestAndValidateChunk(
        prompt: String,
        expectedCount: Int,
        config: TestConfig,
        seenStems: MutableSet<String>
    ): List<MockQuestion> {
        return try {
            val response = withTimeout(60_000L) {
                model.generateContent(prompt)
            }
            val rawJson = response.text
            if (rawJson.isNullOrBlank()) {
                throw IllegalStateException("Gemini returned an empty response.")
            }
            val parsed = parseAndValidateQuestionsJson(rawJson, seenStems)
            if (parsed.isEmpty()) {
                throw IllegalStateException("Generated questions failed structural validation.")
            }
            parsed.take(expectedCount)
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI question generation timed out", e)
            throw IllegalStateException("AI question generation timed out after 60s. Please try again.", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI question generation failed", e)
            throw e
        }
    }

    private fun buildChunkPlan(subjectCounts: Map<String, Int>): List<Map<String, Int>> {
        val chunks = mutableListOf<Map<String, Int>>()
        var current = linkedMapOf<String, Int>()
        var currentTotal = 0

        for ((subject, totalForSubject) in subjectCounts) {
            var remaining = totalForSubject
            while (remaining > 0) {
                val space = CHUNK_SIZE - currentTotal
                if (space <= 0) {
                    chunks.add(current)
                    current = linkedMapOf()
                    currentTotal = 0
                    continue
                }
                val take = minOf(remaining, space)
                current[subject] = (current[subject] ?: 0) + take
                currentTotal += take
                remaining -= take
                if (currentTotal >= CHUNK_SIZE) {
                    chunks.add(current)
                    current = linkedMapOf()
                    currentTotal = 0
                }
            }
        }
        if (current.isNotEmpty()) {
            chunks.add(current)
        }
        return chunks
    }

    private fun buildBanglaPrompt(
        config: TestConfig,
        chunkCounts: Map<String, Int>,
        recentStems: List<String>
    ): String {
        val total = chunkCounts.values.sum()
        val distributionDesc = chunkCounts.entries.joinToString(", ") { "${it.key}: ${it.value}" }
        val trackDesc = when (config.mode) {
            TestMode.MEDICAL -> "Bangladesh Medical (MBBS/BDS) Admission Test"
            TestMode.VARSITY_MODEL_TEST -> "Bangladesh University Admission Model Test (${config.titleEn}, Unit ${config.unitId}, Session ${config.sessionYear})"
            TestMode.VARSITY_QUESTION_BANK -> "Bangladesh University Admission Practice (${config.titleEn}, Unit ${config.unitId})"
        }
        val difficultyDesc = when (config.difficulty) {
            QuestionDifficulty.EASY -> "Easy (HSC foundation clarity)"
            QuestionDifficulty.MODERATE -> "Moderate (Standard HSC + admission conceptual)"
            QuestionDifficulty.HARD -> "Hard (Deep analytical & tricky admission level)"
            QuestionDifficulty.ADMISSION_STANDARD -> "Actual Bangladesh Admission Standard (NCTB HSC + competitive exam rigor)"
            QuestionDifficulty.MIXED -> "Mixed (balanced Easy, Moderate, and Admission Standard)"
        }
        val avoidDuplicates = if (recentStems.isEmpty()) "" else {
            "\nAvoid repeating or paraphrasing these already generated questions:\n- " +
                recentStems.joinToString("\n- ")
        }

        return """
            You are an expert Bangladesh university and medical admission test setter (NCTB HSC syllabus & competitive admission standard).
            Generate EXACTLY $total unique multiple-choice questions for: $trackDesc.
            Subject distribution required: $distributionDesc.
            Difficulty level: $difficultyDesc.

            STRICT LANGUAGE & QUALITY RULES:
            1. ALL question stems (`questionBn`), ALL four options (`optionA`, `optionB`, `optionC`, `optionD`), topic names (`topic`), and detailed explanations (`explanationBn`) MUST BE WRITTEN IN BANGLA.
            2. For English subject questions (grammar, vocabulary, comprehension), keep the target English word, sentence, or short passage in English, while writing the instruction/question framing and full explanation in clear Bangla.
            3. For Science/Medical/Engineering subjects, scientific terminology may include the English term in parentheses alongside Bangla (e.g., অভিস্রবণ (Osmosis)).
            4. Every question must have 4 distinct, plausible options (A, B, C, D) and EXACTLY ONE unambiguous correct answer (`correctOption` in {"A","B","C","D"}).
            5. Provide a thorough, educational Bangla explanation (`explanationBn`) explaining why the correct option is right.
            6. No duplicate questions, no "All of the above / None of the above" lazy filler, no markdown formatting or code fences.
            $avoidDuplicates
        """.trimIndent()
    }

    companion object {
        fun parseAndValidateQuestionsJson(
            jsonString: String,
            seenStems: MutableSet<String> = mutableSetOf()
        ): List<MockQuestion> {
            val root = JSONObject(jsonString)
            val arr = root.getJSONArray("questions")
            val validList = mutableListOf<MockQuestion>()

            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val subject = obj.optString("subject", "").trim()
                val topic = obj.optString("topic", "").trim()
                val questionBn = obj.optString("questionBn", "").trim()
                val optionA = obj.optString("optionA", "").trim()
                val optionB = obj.optString("optionB", "").trim()
                val optionC = obj.optString("optionC", "").trim()
                val optionD = obj.optString("optionD", "").trim()
                val correctOption = obj.optString("correctOption", "").trim().uppercase()
                val explanationBn = obj.optString("explanationBn", "").trim()

                if (subject.isBlank() || questionBn.isBlank() || explanationBn.isBlank()) continue
                if (optionA.isBlank() || optionB.isBlank() || optionC.isBlank() || optionD.isBlank()) continue
                if (correctOption !in setOf("A", "B", "C", "D")) continue

                val distinctOptions = setOf(optionA, optionB, optionC, optionD)
                if (distinctOptions.size < 4) continue

                val normalizedStem = questionBn.lowercase().replace("\\s+".toRegex(), " ")
                if (!seenStems.add(normalizedStem)) continue

                validList.add(
                    MockQuestion(
                        id = "ai_${validList.size}",
                        index = validList.size,
                        subject = subject,
                        topic = topic.ifBlank { subject },
                        questionBn = questionBn,
                        optionA = optionA,
                        optionB = optionB,
                        optionC = optionC,
                        optionD = optionD,
                        correctOption = correctOption,
                        explanationBn = explanationBn,
                        isAiGenerated = true,
                        sourceInfo = "AI Practice Material (Gemini)"
                    )
                )
            }
            return validList
        }
    }
}
