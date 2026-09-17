package com.example.util

import com.example.data.model.UserProfile
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object HabitBoundaryManager {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
        timeZone = TimeZone.getDefault()
    }

    fun getTodayDateString(): String {
        return dateFormat.format(Date())
    }

    fun getYesterdayDateString(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return dateFormat.format(cal.time)
    }

    data class StreakEvaluation(
        val updatedProfile: UserProfile,
        val freezeUsed: Boolean,
        val streakBroken: Boolean
    )

    /**
     * Evaluates daily streak upon app opening or session start.
     * Uses local calendar timezone, never UTC timestamps.
     */
    fun evaluateDailyActive(
        profile: UserProfile,
        todayDateOverride: String? = null,
        yesterdayDateOverride: String? = null
    ): StreakEvaluation {
        val today = todayDateOverride ?: getTodayDateString()
        val yesterday = yesterdayDateOverride ?: getYesterdayDateString()
        val lastActive = profile.lastActiveDate

        if (lastActive == today) {
            // Already active today
            return StreakEvaluation(profile, freezeUsed = false, streakBroken = false)
        }

        if (lastActive.isBlank() || lastActive == yesterday) {
            // Consecutive day or first day
            val newStreak = if (lastActive == yesterday) profile.streakDays + 1 else profile.streakDays.coerceAtLeast(1)
            val updated = profile.copy(
                streakDays = newStreak,
                lastActiveDate = today,
                todayWordsPracticed = 0,
                isStreakBroken = false,
                // Reset daily quests progress on new day
                questXpEarned = 0,
                questCorrectAnswers = 0,
                questWordsReviewed = 0,
                quest1Claimed = false,
                quest2Claimed = false,
                quest3Claimed = false,
                minutesPracticedToday = 0
            )
            return StreakEvaluation(updated, freezeUsed = false, streakBroken = false)
        }

        // Missed at least one day! Check for streak freeze
        if (profile.streakFreezeCount > 0) {
            // Auto-consume streak freeze
            val updated = profile.copy(
                streakFreezeCount = profile.streakFreezeCount - 1,
                lastActiveDate = today,
                todayWordsPracticed = 0,
                isStreakBroken = false,
                questXpEarned = 0,
                questCorrectAnswers = 0,
                questWordsReviewed = 0,
                quest1Claimed = false,
                quest2Claimed = false,
                quest3Claimed = false
            )
            return StreakEvaluation(updated, freezeUsed = true, streakBroken = false)
        } else {
            // Streak broken
            val updated = profile.copy(
                isStreakBroken = true,
                lastActiveDate = today,
                todayWordsPracticed = 0,
                questXpEarned = 0,
                questCorrectAnswers = 0,
                questWordsReviewed = 0,
                quest1Claimed = false,
                quest2Claimed = false,
                quest3Claimed = false
            )
            return StreakEvaluation(updated, freezeUsed = false, streakBroken = true)
        }
    }

    /**
     * Attempts to repair a broken streak for 200 gems.
     */
    fun repairStreak(profile: UserProfile): UserProfile? {
        if (profile.gems < 200) return null
        return profile.copy(
            gems = profile.gems - 200,
            isStreakBroken = false,
            streakDays = profile.streakDays + 1,
            streakRepairedCount = profile.streakRepairedCount + 1
        )
    }
}
