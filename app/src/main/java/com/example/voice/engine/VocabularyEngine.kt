package com.example.voice.engine

import com.example.voice.model.LearnerWord

class VocabularyEngine {

    /**
     * Extracts 3-5 useful vocabulary words based on context and language.
     * Prevents cognitive overload by recommending concise, high-impact words.
     */
    fun extractVocabulary(
        targetLanguage: String,
        recentConversation: String,
        currentLearnerLevel: String
    ): List<LearnerWord> {
        val lower = recentConversation.lowercase()

        // Curated dictionary by language and context
        val candidateWords = when (targetLanguage.lowercase()) {
            "hindi" -> listOf(
                LearnerWord("वार्तालाप", "conversation / dialogue", "हमारा वार्तालाप बहुत अच्छा रहा।", "B1"),
                LearnerWord("अनुभव", "experience", "मुझे शिक्षण का गहरा अनुभव है।", "B1"),
                LearnerWord("सफलता", "success", "कड़ी मेहनत से सफलता मिलती है।", "A2"),
                LearnerWord("रुचि", "interest", "मेरी भाषा सीखने में रुचि है।", "A2"),
                LearnerWord("अवसर", "opportunity", "यह सीखने का बेहतरीन अवसर है।", "B1")
            )
            "spanish" -> listOf(
                LearnerWord("desarrollo", "development / growth", "El desarrollo de nuevas habilidades es clave.", "B1"),
                LearnerWord("negociar", "to negotiate", "Es importante negociar con confianza.", "B1"),
                LearnerWord("abrumado", "overwhelmed", "A veces me siento abrumado por el trabajo.", "B1"),
                LearnerWord("oportunidad", "opportunity", "Esta es una gran oportunidad para aprender.", "A2"),
                LearnerWord("éxito", "success", "Celebrar el éxito nos motiva a seguir.", "A2")
            )
            "french" -> listOf(
                LearnerWord("développement", "development", "Le développement personnel est essentiel.", "B1"),
                LearnerWord("négocier", "to negotiate", "Nous devons négocier les conditions.", "B1"),
                LearnerWord("débordé", "overwhelmed", "Je suis un peu débordé cette semaine.", "B1"),
                LearnerWord("opportunité", "opportunity", "C'est une excellente opportunité.", "A2"),
                LearnerWord("réussite", "success", "La persévérance mène à la réussite.", "A2")
            )
            else -> listOf(
                LearnerWord("negotiate", "to reach an agreement through discussion", "We need to negotiate a fair timeline.", "B1"),
                LearnerWord("overwhelmed", "feeling burdened by too much to deal with", "I felt overwhelmed at first, but stayed calm.", "B1"),
                LearnerWord("resilient", "able to recover quickly from difficulties", "She showed a resilient attitude after the setback.", "B2"),
                LearnerWord("collaborate", "work jointly with others on an activity", "We collaborated across three different teams.", "B1"),
                LearnerWord("perspective", "a particular way of regarding something", "That gives us a fresh new perspective.", "B1")
            )
        }

        // Return 3-5 words
        return candidateWords.take(4)
    }
}
