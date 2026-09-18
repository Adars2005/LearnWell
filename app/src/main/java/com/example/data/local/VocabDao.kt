package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ReviewLog
import com.example.data.model.UserProfile
import com.example.data.model.VocabWord
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabDao {

    @Query("SELECT * FROM vocab_words WHERE language = :language ORDER BY id ASC")
    fun getWordsByLanguage(language: String): Flow<List<VocabWord>>

    @Query("SELECT * FROM vocab_words WHERE language = :language AND (:level = 'ALL' OR level = :level) ORDER BY id ASC")
    fun getWordsByLanguageAndLevel(language: String, level: String): Flow<List<VocabWord>>

    @Query("SELECT * FROM vocab_words WHERE language = :language AND (lastReviewedTimestamp = 0 OR nextReviewTimestamp <= :currentTime) ORDER BY nextReviewTimestamp ASC")
    fun getWordsDueForReview(language: String, currentTime: Long): Flow<List<VocabWord>>

    @Query("SELECT * FROM vocab_words WHERE language = :language AND isMistake = 1 ORDER BY mistakeCount DESC")
    fun getMistakeWords(language: String): Flow<List<VocabWord>>

    @Query("SELECT * FROM vocab_words WHERE language = :language AND repetitions = 0 LIMIT :limit")
    suspend fun getNewWords(language: String, limit: Int): List<VocabWord>

    @Query("SELECT * FROM vocab_words WHERE language = :language AND word = :word LIMIT 1")
    suspend fun getWordByLanguageAndText(language: String, word: String): VocabWord?

    @Query("SELECT * FROM vocab_words WHERE language = :language AND (mistakeCount > 0 OR stability < 1.5) ORDER BY mistakeCount DESC, stability ASC LIMIT :limit")
    fun getWeakestWords(language: String, limit: Int = 10): Flow<List<VocabWord>>

    @Query("SELECT COUNT(*) FROM vocab_words")
    suspend fun getWordCount(): Int

    @Query("SELECT COUNT(*) FROM vocab_words WHERE language = :language")
    suspend fun getWordCountForLanguage(language: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(words: List<VocabWord>)

    @Update
    suspend fun updateWord(word: VocabWord)

    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getUserProfileOnce(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)

    // Upsert prevents an early onboarding/profile interaction from being lost while the
    // asynchronous first-run seed is still completing.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateUserProfile(profile: UserProfile)

    // Append-only Review Log for analytics & future sync
    @Insert
    suspend fun insertReviewLog(log: ReviewLog)

    @Query("SELECT * FROM review_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentReviewLogs(limit: Int = 50): Flow<List<ReviewLog>>

    @Query("SELECT COUNT(*) FROM review_logs WHERE timestamp >= :sinceTimestamp")
    suspend fun getReviewCountSince(sinceTimestamp: Long): Int
}
