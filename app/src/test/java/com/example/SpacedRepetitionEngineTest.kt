package com.example

import com.example.data.model.VocabWord
import com.example.data.srs.FsrsRating
import com.example.data.srs.SpacedRepetitionEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpacedRepetitionEngineTest {

    @Test
    fun testFsrsRatingsUpdateStabilityAndInterval() {
        val word = VocabWord(
            language = "Spanish",
            word = "manzana",
            phonetic = "mahn-SAH-nah",
            translation = "apple",
            category = "Food",
            exampleSentence = "Como una manzana.",
            exampleTranslation = "I eat an apple."
        )

        // Rating: GOOD
        val goodReview = SpacedRepetitionEngine.processRating(word, FsrsRating.GOOD)
        assertEquals(1, goodReview.repetitions)
        assertTrue(goodReview.stability >= 3.0f)
        assertTrue(goodReview.intervalDays >= 3)
        assertFalse(goodReview.isMistake)

        // Rating: EASY -> higher stability than GOOD
        val easyReview = SpacedRepetitionEngine.processRating(word, FsrsRating.EASY)
        assertTrue(easyReview.stability > goodReview.stability)
        assertTrue(easyReview.intervalDays > goodReview.intervalDays)

        // Rating: AGAIN -> lapse, mistake marked
        val againReview = SpacedRepetitionEngine.processRating(word, FsrsRating.AGAIN)
        assertEquals(0, againReview.repetitions)
        assertTrue(againReview.isMistake)
        assertEquals(1, againReview.mistakeCount)
        assertTrue(againReview.stability < 1.0f)
    }

    @Test
    fun testRetrievabilityFormulaDecay() {
        val stability = 10.0f
        // Right after review (elapsed = 0 days), retrievability = 1.0 (100%)
        val r0 = SpacedRepetitionEngine.calculateRetrievability(0.0f, stability)
        assertEquals(1.0f, r0, 0.01f)

        // After 10 days (elapsed = stability), retrievability is near 0.90 (90%)
        val r10 = SpacedRepetitionEngine.calculateRetrievability(10.0f, stability)
        assertTrue(r10 in 0.88f..0.92f)

        // After 50 days, retrievability has decayed significantly
        val r50 = SpacedRepetitionEngine.calculateRetrievability(50.0f, stability)
        assertTrue(r50 < r10)
        assertTrue(r50 > 0.40f)
    }

    @Test
    fun testNextIntervalDaysRetentionParameter() {
        val stability = 10.0f
        val interval90 = SpacedRepetitionEngine.nextIntervalDays(stability, desiredRetention = 0.90f)
        val interval80 = SpacedRepetitionEngine.nextIntervalDays(stability, desiredRetention = 0.80f)

        // Lower retention expectation gives longer intervals before review
        assertTrue(interval80 > interval90)
    }

    @Test
    fun testConsecutiveGoodReviewsIncreaseInterval() {
        var word = VocabWord(
            language = "Hindi",
            word = "किताब",
            phonetic = "kitaab",
            translation = "book",
            category = "Basics",
            exampleSentence = "वह किताब है।",
            exampleTranslation = "That is a book."
        )

        val initialDay = 1_000_000_000L
        word = SpacedRepetitionEngine.processRating(word, FsrsRating.GOOD, currentTime = initialDay)
        val firstInterval = word.intervalDays

        // Advance time by firstInterval days
        val nextTime = initialDay + (firstInterval * 24L * 3600L * 1000L)
        word = SpacedRepetitionEngine.processRating(word, FsrsRating.GOOD, currentTime = nextTime)
        val secondInterval = word.intervalDays

        assertTrue(secondInterval > firstInterval)
        assertEquals(2, word.repetitions)
        assertEquals(2, word.consecutiveCorrect)
    }
}
