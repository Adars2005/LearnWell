package com.example

import com.example.data.model.UserProfile
import com.example.util.HabitBoundaryManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HabitBoundaryManagerTest {

    @Test
    fun testSameDayActivityPreservesStreak() {
        val today = "2026-09-17"
        val profile = UserProfile(
            lastActiveDate = today,
            streakDays = 5,
            isStreakBroken = false
        )

        val result = HabitBoundaryManager.evaluateDailyActive(profile, todayDateOverride = today)
        assertEquals(5, result.updatedProfile.streakDays)
        assertFalse(result.freezeUsed)
        assertFalse(result.streakBroken)
    }

    @Test
    fun testConsecutiveDayIncrementsStreak() {
        val yesterday = "2026-09-16"
        val today = "2026-09-17"
        val profile = UserProfile(
            lastActiveDate = yesterday,
            streakDays = 5,
            isStreakBroken = false
        )

        val result = HabitBoundaryManager.evaluateDailyActive(
            profile,
            todayDateOverride = today,
            yesterdayDateOverride = yesterday
        )
        assertEquals(6, result.updatedProfile.streakDays)
        assertEquals(today, result.updatedProfile.lastActiveDate)
        assertFalse(result.freezeUsed)
        assertFalse(result.streakBroken)
    }

    @Test
    fun testMissedDayUsesStreakFreeze() {
        val twoDaysAgo = "2026-09-15"
        val yesterday = "2026-09-16"
        val today = "2026-09-17"
        val profile = UserProfile(
            lastActiveDate = twoDaysAgo,
            streakDays = 10,
            streakFreezeCount = 2,
            isStreakBroken = false
        )

        val result = HabitBoundaryManager.evaluateDailyActive(
            profile,
            todayDateOverride = today,
            yesterdayDateOverride = yesterday
        )
        assertEquals(10, result.updatedProfile.streakDays)
        assertEquals(1, result.updatedProfile.streakFreezeCount)
        assertTrue(result.freezeUsed)
        assertFalse(result.streakBroken)
    }

    @Test
    fun testMissedDayWithoutFreezeBreaksStreakGracefully() {
        val threeDaysAgo = "2026-09-14"
        val yesterday = "2026-09-16"
        val today = "2026-09-17"
        val profile = UserProfile(
            lastActiveDate = threeDaysAgo,
            streakDays = 12,
            streakFreezeCount = 0,
            isStreakBroken = false
        )

        val result = HabitBoundaryManager.evaluateDailyActive(
            profile,
            todayDateOverride = today,
            yesterdayDateOverride = yesterday
        )
        assertTrue(result.updatedProfile.isStreakBroken)
        assertTrue(result.streakBroken)
        assertFalse(result.freezeUsed)
    }
}
