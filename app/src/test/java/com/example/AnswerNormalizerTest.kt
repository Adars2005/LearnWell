package com.example

import com.example.util.AnswerNormalizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnswerNormalizerTest {

    @Test
    fun testNormalizeStripsPunctuationAndDiacritics() {
        assertEquals("hola", AnswerNormalizer.normalize("¡Hola!"))
        assertEquals("garcon", AnswerNormalizer.normalize("Garçon"))
        assertEquals("uber", AnswerNormalizer.normalize("Über"))
        assertEquals("manana", AnswerNormalizer.normalize("Mañana"))
        assertEquals("hello world", AnswerNormalizer.normalize("  Hello,   World...  "))
    }

    @Test
    fun testLenientMatchSuccesses() {
        // Accents omitted
        assertTrue(AnswerNormalizer.isLenientMatch("garcon", "garçon"))
        // Case insensitive
        assertTrue(AnswerNormalizer.isLenientMatch("BONJOUR", "bonjour"))
        // Punctuation and inverted marks omitted
        assertTrue(AnswerNormalizer.isLenientMatch("Como estas", "¿Cómo estás?"))
        // Extra spaces
        assertTrue(AnswerNormalizer.isLenientMatch("je   suis   etudiant", "Je suis étudiant."))
    }

    @Test
    fun testLenientMatchFailsOnDifferentWords() {
        assertFalse(AnswerNormalizer.isLenientMatch("perro", "gato"))
        assertFalse(AnswerNormalizer.isLenientMatch("bonjour", "au revoir"))
        assertFalse(AnswerNormalizer.isLenientMatch("wasser", "brot"))
    }

    @Test
    fun testLevenshteinDistance() {
        assertEquals(0, AnswerNormalizer.levenshteinDistance("apple", "apple"))
        assertEquals(1, AnswerNormalizer.levenshteinDistance("apple", "aple"))
        assertEquals(1, AnswerNormalizer.levenshteinDistance("book", "boot"))
        assertEquals(3, AnswerNormalizer.levenshteinDistance("kitten", "sitting"))
    }
}
