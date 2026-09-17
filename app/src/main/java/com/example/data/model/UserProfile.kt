package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey
    val id: Int = 1,
    val targetLanguage: String = "Hindi",
    val dailyGoalMinutes: Int = 10,
    val dailyGoalWords: Int = 10,
    val streakDays: Int = 3,
    val streakFreezeCount: Int = 1,
    val isStreakBroken: Boolean = false,
    val streakRepairedCount: Int = 0,
    val hearts: Int = 5,
    val maxHearts: Int = 5,
    val relaxedMode: Boolean = false, // "Keep learning without hearts"
    val totalXp: Int = 240,
    val gems: Int = 350,
    val todayWordsPracticed: Int = 0,
    val lastActiveDate: String = "", // Local Date "YYYY-MM-DD"
    val hasCompletedOnboarding: Boolean = false,
    val learningLevel: String = "A1", // "A1", "A2", "B1"
    val soundEnabled: Boolean = true,
    val darkTheme: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val reminderHour: Int = 19,
    // Garden Customizations
    val gardenPot: String = "Terracotta", // "Terracotta", "Ceramic", "Golden"
    val gardenCompanion: String = "Butterflies", // "Butterflies", "Honeybees", "Hummingbirds"
    val gardenAtmosphere: String = "Sunny Meadow", // "Sunny Meadow", "Twilight Grove", "Cherry Blossom"
    // Daily Quests Progress
    val questXpEarned: Int = 0, // Target: 30 XP
    val questCorrectAnswers: Int = 0, // Target: 8 correct
    val questWordsReviewed: Int = 0, // Target: 5 reviews
    val quest1Claimed: Boolean = false,
    val quest2Claimed: Boolean = false,
    val quest3Claimed: Boolean = false,
    val minutesPracticedToday: Int = 4
)

