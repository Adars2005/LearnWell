package com.example.voice.engine

import com.example.voice.model.GrammarCorrection
import com.example.voice.model.VoiceMode

class GrammarCorrectionEngine {

    /**
     * Checks user text for common grammar patterns according to mode rules:
     * - FRIEND: Minimal corrections (rarely interrupts).
     * - TUTOR: Frequent corrections with explanations.
     * - INTERVIEW: No interruptions during interview.
     * - SPEAKING_PRACTICE: Collected silently for post-session review.
     */
    fun analyzeGrammar(userText: String, mode: VoiceMode): GrammarCorrection? {
        if (mode == VoiceMode.INTERVIEW) {
            // Never interrupt during interview
            return null
        }

        val textLower = userText.trim().lowercase()

        // Common ESL / Language learner mistake heuristics
        val corrections = listOf(
            GrammarRule(
                regex = Regex("\\bi go yesterday\\b"),
                original = "I go yesterday",
                correction = "I went yesterday",
                errorType = "past_tense",
                explanation = "Use the past form 'went' because the action happened yesterday."
            ),
            GrammarRule(
                regex = Regex("\\bi have (a )?many books\\b"),
                original = "I have a many books",
                correction = "I have many books",
                errorType = "quantifier",
                explanation = "'Many' is used with plural nouns without the indefinite article 'a'."
            ),
            GrammarRule(
                regex = Regex("\\bhe don'?t like\\b"),
                original = "he don't like",
                correction = "he doesn't like",
                errorType = "subject_verb_agreement",
                explanation = "Third-person singular subjects take 'doesn't', not 'don't'."
            ),
            GrammarRule(
                regex = Regex("\\bshe go to\\b"),
                original = "she go to",
                correction = "she goes to",
                errorType = "third_person_singular",
                explanation = "Remember to add '-s' or '-es' for third-person singular present tense."
            ),
            GrammarRule(
                regex = Regex("\\bi am agree\\b"),
                original = "I am agree",
                correction = "I agree",
                errorType = "stative_verb",
                explanation = "'Agree' is already a verb; say 'I agree' rather than 'I am agree'."
            ),
            GrammarRule(
                regex = Regex("\\bmore better\\b"),
                original = "more better",
                correction = "better / much better",
                errorType = "double_comparative",
                explanation = "'Better' is already comparative. Use 'much better' for emphasis."
            ),
            GrammarRule(
                regex = Regex("\\bi did not went\\b"),
                original = "I did not went",
                correction = "I did not go",
                errorType = "auxiliary_verb",
                explanation = "After the auxiliary 'did not', use the base form of the verb 'go'."
            )
        )

        for (rule in corrections) {
            if (rule.regex.containsMatchIn(textLower)) {
                if (mode == VoiceMode.FRIEND) {
                    // Friend mode only flags major errors rarely (e.g. 50% probability or stative)
                    if (rule.errorType == "stative_verb" || rule.errorType == "past_tense") {
                        return GrammarCorrection(
                            original = rule.original,
                            correction = rule.correction,
                            errorType = rule.errorType,
                            explanation = rule.explanation
                        )
                    }
                    return null
                }
                return GrammarCorrection(
                    original = rule.original,
                    correction = rule.correction,
                    errorType = rule.errorType,
                    explanation = rule.explanation
                )
            }
        }

        return null
    }

    private data class GrammarRule(
        val regex: Regex,
        val original: String,
        val correction: String,
        val errorType: String,
        val explanation: String
    )
}
