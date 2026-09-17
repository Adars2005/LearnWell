package com.example.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class TtsManager(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
        } else {
            Log.e("TtsManager", "TTS initialization failed with code: $status")
        }
    }

    fun speak(text: String, language: String = "Hindi", isSlow: Boolean = false) {
        if (!isInitialized || tts == null) return

        val locale = when (language.lowercase()) {
            "hindi" -> Locale("hi", "IN")
            "spanish" -> Locale("es", "ES")
            "french" -> Locale("fr", "FR")
            "japanese" -> Locale.JAPAN
            "german" -> Locale.GERMANY
            else -> Locale.US
        }

        try {
            val res = tts?.setLanguage(locale)
            if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to default
                tts?.language = Locale.US
            }
            tts?.setSpeechRate(if (isSlow) 0.5f else 1.0f)
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "vocab_audio")
        } catch (e: Exception) {
            Log.e("TtsManager", "Error in speak: ${e.message}")
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
