package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ReviewLog
import com.example.data.model.UserProfile
import com.example.data.model.VocabWord
import com.example.voice.data.ConversationMessageEntity
import com.example.voice.data.ConversationSummaryEntity
import com.example.voice.data.GrammarErrorEntity
import com.example.voice.data.InterviewAnswerEntity
import com.example.voice.data.InterviewSessionEntity
import com.example.voice.data.LearnerProfileEntity
import com.example.voice.data.SpeakingEvaluationEntity
import com.example.voice.data.VoiceDao
import com.example.voice.data.VoiceSessionEntity
import com.example.voice.data.VoiceUsageLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        VocabWord::class,
        UserProfile::class,
        ReviewLog::class,
        VoiceSessionEntity::class,
        ConversationMessageEntity::class,
        ConversationSummaryEntity::class,
        LearnerProfileEntity::class,
        GrammarErrorEntity::class,
        SpeakingEvaluationEntity::class,
        InterviewSessionEntity::class,
        InterviewAnswerEntity::class,
        VoiceUsageLogEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun vocabDao(): VocabDao
    abstract fun voiceDao(): VoiceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "linguabloom_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate initial vocabulary on background thread
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = getDatabase(context).vocabDao()
                                dao.insertAll(InitialVocabData.getInitialWords())
                                dao.insertUserProfile(
                                    UserProfile(
                                        id = 1,
                                        targetLanguage = "Hindi",
                                        dailyGoalMinutes = 10,
                                        dailyGoalWords = 10,
                                        streakDays = 3,
                                        hearts = 5,
                                        totalXp = 120,
                                        gems = 280,
                                        todayWordsPracticed = 0,
                                        hasCompletedOnboarding = false
                                    )
                                )
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
