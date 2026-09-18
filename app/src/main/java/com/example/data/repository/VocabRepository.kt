package com.example.data.repository

import android.content.Context
import com.example.data.local.InitialVocabData
import com.example.data.local.VocabAssetLoader
import com.example.data.local.VocabDao
import com.example.data.model.ReviewLog
import com.example.data.model.UserProfile
import com.example.data.model.VocabWord
import com.example.data.srs.FsrsRating
import com.example.data.srs.SpacedRepetitionEngine
import com.example.util.HabitBoundaryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class VocabRepository(private val vocabDao: VocabDao) {

    fun getWordsByLanguage(language: String): Flow<List<VocabWord>> {
        return vocabDao.getWordsByLanguage(language)
    }

    fun getWordsByLanguageAndLevel(language: String, level: String): Flow<List<VocabWord>> {
        return vocabDao.getWordsByLanguageAndLevel(language, level)
    }

    fun getDueWords(language: String): Flow<List<VocabWord>> {
        return vocabDao.getWordsDueForReview(language, System.currentTimeMillis())
    }

    fun getMistakeWords(language: String): Flow<List<VocabWord>> {
        return vocabDao.getMistakeWords(language)
    }

    fun getWeakestWords(language: String, limit: Int = 10): Flow<List<VocabWord>> {
        return vocabDao.getWeakestWords(language, limit)
    }

    fun getUserProfile(): Flow<UserProfile?> {
        return vocabDao.getUserProfile()
    }

    fun getRecentReviewLogs(limit: Int = 50): Flow<List<ReviewLog>> {
        return vocabDao.getRecentReviewLogs(limit)
    }

    suspend fun ensureSeeded(context: Context) = withContext(Dispatchers.IO) {
        val count = vocabDao.getWordCount()
        if (count == 0) {
            val initialWords = VocabAssetLoader.loadInitialDecks(context)
            vocabDao.insertAll(initialWords)
        } else {
            // A learner upgrading the app receives starter words for newly added languages
            // without duplicating or overwriting their existing progress.
            val bundled = InitialVocabData.getInitialWords()
            for ((language, words) in bundled.groupBy { it.language }) {
                if (vocabDao.getWordCountForLanguage(language) == 0) vocabDao.insertAll(words)
            }
        }
        // Seed the profile independently from vocabulary. This protects existing learners and
        // also recovers cleanly if a prior first-run was interrupted between the two inserts.
        if (vocabDao.getUserProfileOnce() == null) {
            vocabDao.insertUserProfile(
                UserProfile(
                    id = 1,
                    targetLanguage = "Hindi",
                    dailyGoalMinutes = 10,
                    dailyGoalWords = 10,
                    streakDays = 3,
                    streakFreezeCount = 1,
                    hearts = 5,
                    totalXp = 120,
                    gems = 300,
                    todayWordsPracticed = 0,
                    hasCompletedOnboarding = false,
                    lastActiveDate = HabitBoundaryManager.getTodayDateString()
                )
            )
        }
    }

    suspend fun updateWordSRS(
        word: VocabWord,
        isCorrect: Boolean,
        isFastRecall: Boolean = false
    ): VocabWord = withContext(Dispatchers.IO) {
        val updated = SpacedRepetitionEngine.processReview(word, isCorrect, isFastRecall)
        vocabDao.updateWord(updated)
        updated
    }

    suspend fun updateWordRating(
        word: VocabWord,
        rating: FsrsRating
    ): VocabWord = withContext(Dispatchers.IO) {
        val updated = SpacedRepetitionEngine.processRating(word, rating)
        vocabDao.updateWord(updated)
        updated
    }

    suspend fun logReview(
        wordId: Int,
        rating: Int,
        exerciseType: String,
        isCorrect: Boolean,
        responseTimeMs: Long = 0L
    ) = withContext(Dispatchers.IO) {
        vocabDao.insertReviewLog(
            ReviewLog(
                wordId = wordId,
                rating = rating,
                exerciseType = exerciseType,
                isCorrect = isCorrect,
                responseTimeMs = responseTimeMs
            )
        )
    }

    suspend fun updateUserProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        vocabDao.updateUserProfile(profile)
    }

    suspend fun decrementHeart(currentProfile: UserProfile): UserProfile = withContext(Dispatchers.IO) {
        if (currentProfile.relaxedMode) return@withContext currentProfile
        val newHearts = (currentProfile.hearts - 1).coerceAtLeast(0)
        val updated = currentProfile.copy(hearts = newHearts)
        vocabDao.updateUserProfile(updated)
        updated
    }

    suspend fun refillHearts(currentProfile: UserProfile): UserProfile = withContext(Dispatchers.IO) {
        val updated = currentProfile.copy(hearts = currentProfile.maxHearts)
        vocabDao.updateUserProfile(updated)
        updated
    }

    suspend fun refillHeartsWithGems(currentProfile: UserProfile): UserProfile? = withContext(Dispatchers.IO) {
        if (currentProfile.gems < 100) return@withContext null
        val updated = currentProfile.copy(
            hearts = currentProfile.maxHearts,
            gems = currentProfile.gems - 100
        )
        vocabDao.updateUserProfile(updated)
        updated
    }

    suspend fun buyStreakFreeze(currentProfile: UserProfile): UserProfile? = withContext(Dispatchers.IO) {
        if (currentProfile.gems < 100) return@withContext null
        val updated = currentProfile.copy(
            streakFreezeCount = currentProfile.streakFreezeCount + 1,
            gems = currentProfile.gems - 100
        )
        vocabDao.updateUserProfile(updated)
        updated
    }

    suspend fun repairStreak(currentProfile: UserProfile): UserProfile? = withContext(Dispatchers.IO) {
        val repaired = HabitBoundaryManager.repairStreak(currentProfile) ?: return@withContext null
        vocabDao.updateUserProfile(repaired)
        repaired
    }

    suspend fun addXpAndProgress(
        currentProfile: UserProfile,
        xpEarned: Int,
        wordsCount: Int,
        correctCount: Int
    ): UserProfile = withContext(Dispatchers.IO) {
        val updated = currentProfile.copy(
            totalXp = currentProfile.totalXp + xpEarned,
            todayWordsPracticed = currentProfile.todayWordsPracticed + wordsCount,
            questXpEarned = currentProfile.questXpEarned + xpEarned,
            questCorrectAnswers = currentProfile.questCorrectAnswers + correctCount,
            questWordsReviewed = currentProfile.questWordsReviewed + wordsCount,
            minutesPracticedToday = currentProfile.minutesPracticedToday + (wordsCount * 1).coerceAtLeast(1)
        )
        vocabDao.updateUserProfile(updated)
        updated
    }
}
