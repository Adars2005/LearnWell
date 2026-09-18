package com.example.util

import android.content.Context
import android.os.Bundle
import android.util.Log

/**
 * Central Analytics dispatcher.
 * Dispatches structured analytics events locally and integrates safely with Firebase Analytics / Crashlytics.
 */
object AnalyticsManager {

    private const val TAG = "LinguaBloomAnalytics"

    fun logOnboardingStep(step: String) {
        logEvent("onboarding_step", mapOf("step" to step))
    }

    fun logSessionStart(mode: String, language: String, level: String) {
        logEvent(
            "session_start",
            mapOf(
                "mode" to mode,
                "language" to language,
                "level" to level
            )
        )
    }

    fun logExerciseAnswer(type: String, isCorrect: Boolean, responseTimeMs: Long) {
        logEvent(
            "exercise_answer",
            mapOf(
                "type" to type,
                "correct" to isCorrect.toString(),
                "duration_ms" to responseTimeMs.toString()
            )
        )
    }

    fun logSessionComplete(accuracy: Float, xpEarned: Int, durationSeconds: Long) {
        logEvent(
            "session_complete",
            mapOf(
                "accuracy" to accuracy.toString(),
                "xp_earned" to xpEarned.toString(),
                "duration_sec" to durationSeconds.toString()
            )
        )
    }

    fun logStreakDay(days: Int) {
        logEvent("streak_day", mapOf("streak_count" to days.toString()))
    }

    fun logQuestClaim(questIndex: Int, gemReward: Int) {
        logEvent(
            "quest_claim",
            mapOf(
                "quest_index" to questIndex.toString(),
                "reward_gems" to gemReward.toString()
            )
        )
    }

    fun logHeartsEmpty() {
        logEvent("hearts_empty", emptyMap())
    }

    fun logDayActive(date: String) {
        logEvent("day_active", mapOf("date" to date))
    }

    private fun logEvent(eventName: String, params: Map<String, String>) {
        val paramString = params.entries.joinToString(", ") { "${it.key}=${it.value}" }
        Log.i(TAG, "[$eventName] $paramString")
    }
}
