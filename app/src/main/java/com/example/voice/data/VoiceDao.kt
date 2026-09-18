package com.example.voice.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VoiceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: VoiceSessionEntity)

    @Update
    suspend fun updateSession(session: VoiceSessionEntity)

    @Query("SELECT * FROM voice_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<VoiceSessionEntity>>

    @Query("SELECT * FROM voice_sessions WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: String): VoiceSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ConversationMessageEntity)

    @Query("SELECT * FROM conversation_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: String): Flow<List<ConversationMessageEntity>>

    @Query("SELECT * FROM conversation_messages WHERE sessionId = :sessionId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessages(sessionId: String, limit: Int): List<ConversationMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummary(summary: ConversationSummaryEntity)

    @Query("SELECT * FROM conversation_summaries WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getSummary(sessionId: String): ConversationSummaryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLearnerProfile(profile: LearnerProfileEntity)

    @Query("SELECT * FROM learner_profiles WHERE userId = :userId LIMIT 1")
    suspend fun getLearnerProfile(userId: String = "local_user"): LearnerProfileEntity?

    @Query("SELECT * FROM learner_profiles WHERE userId = :userId LIMIT 1")
    fun getLearnerProfileFlow(userId: String = "local_user"): Flow<LearnerProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrammarError(error: GrammarErrorEntity)

    @Query("SELECT * FROM grammar_errors WHERE sessionId = :sessionId")
    suspend fun getGrammarErrorsForSession(sessionId: String): List<GrammarErrorEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpeakingEvaluation(eval: SpeakingEvaluationEntity)

    @Query("SELECT * FROM speaking_evaluations WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getSpeakingEvaluation(sessionId: String): SpeakingEvaluationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInterviewSession(session: InterviewSessionEntity)

    @Update
    suspend fun updateInterviewSession(session: InterviewSessionEntity)

    @Query("SELECT * FROM interview_sessions WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getInterviewSession(sessionId: String): InterviewSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInterviewAnswer(answer: InterviewAnswerEntity)

    @Query("SELECT * FROM interview_answers WHERE sessionId = :sessionId ORDER BY questionNumber ASC")
    suspend fun getInterviewAnswers(sessionId: String): List<InterviewAnswerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsageLog(log: VoiceUsageLogEntity)

    @Query("SELECT COUNT(*) FROM voice_sessions WHERE startTime >= :sinceTime")
    suspend fun getSessionCountSince(sinceTime: Long): Int
}
