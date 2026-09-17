package com.example.util

import java.text.Normalizer

object AnswerNormalizer {

    /**
     * Lenient normalization for language learning:
     * - Case-insensitive
     * - Strips diacritics / accents (e.g., 'é' -> 'e', 'ñ' -> 'n', 'ü' -> 'u')
     * - Strips punctuation (commas, periods, exclamation marks, question marks, inverted question marks, etc.)
     * - Collapses multiple spaces
     */
    fun normalize(input: String): String {
        if (input.isBlank()) return ""

        // 1. Lowercase
        val lower = input.lowercase()

        // 2. Decompose Unicode characters (NFD) and remove Mark Nonspacing (\p{Mn})
        val nfdNormalized = Normalizer.normalize(lower, Normalizer.Form.NFD)
        val withoutDiacritics = nfdNormalized.replace(Regex("\\p{Mn}+"), "")

        // 3. Strip punctuation
        val withoutPunctuation = withoutDiacritics.replace(Regex("[.,!?;:\"'\\-_()\\[\\]{}/¿¡«»\u0964\u3001\u3002]"), "")

        // 4. Collapse whitespace
        return withoutPunctuation.replace(Regex("\\s+"), " ").trim()
    }

    /**
     * Checks if user answer matches the expected answer leniently.
     * Never fails on missing accents or capitalizations.
     */
    fun isLenientMatch(userInput: String, expected: String): Boolean {
        val normUser = normalize(userInput)
        val normExpected = normalize(expected)
        if (normUser == normExpected) return true

        // Also allow matching if expected has multiple slash-separated synonyms e.g. "hi / hello"
        val expectedOptions = expected.split("/", ";", "|").map { normalize(it) }
        return normUser in expectedOptions
    }

    /**
     * Levenshtein Distance for minor typo tolerance.
     */
    fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j

        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,       // deletion
                    dp[i][j - 1] + 1,       // insertion
                    dp[i - 1][j - 1] + cost // substitution
                )
            }
        }
        return dp[s1.length][s2.length]
    }
}
