package com.example.voice.provider

import android.util.Log
import com.example.BuildConfig
import com.example.util.TtsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * OpenRouter chat provider behind the existing voice interface.
 * Speech recognition and playback stay on-device by default; only meaningful text turns
 * are sent to OpenRouter, keeping latency and credit consumption low.
 * Includes graceful fallback to LocalFallbackVoiceProvider on network failure or rate limit.
 */
class GeminiLiveProvider(
    private val ttsManager: TtsManager? = null,
    private val fallbackProvider: LocalFallbackVoiceProvider = LocalFallbackVoiceProvider(ttsManager)
) : VoiceProvider {

    override val providerName: String = "openrouter-text-with-local-audio"

    private val audioFlow = MutableSharedFlow<AudioChunk>(extraBufferCapacity = 64)
    private val transcriptFlow = MutableSharedFlow<TranscriptChunk>(extraBufferCapacity = 64)
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private var isConnected = false
    private var currentConfig: ProviderConfig? = null
    private var isStreaming = false
    private var isInterrupted = false
    private var isUsingFallback = false
    private var fallbackForwardingStarted = false
    private var activeSessionId = ""

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    // Recent message window for conversational memory (Level 2)
    private val conversationHistory = mutableListOf<JSONObject>()

    override suspend fun connect(sessionId: String, config: ProviderConfig) {
        activeSessionId = sessionId
        isConnected = true
        isInterrupted = false
        isUsingFallback = false
        currentConfig = config
        conversationHistory.clear()

        val apiKey = getApiKey()

        if (apiKey.isBlank() || apiKey == "YOUR_OPENROUTER_API_KEY") {
            Log.w("OpenRouterVoice", "No OpenRouter key found; using the on-device conversation fallback.")
            activateFallback(sessionId, config)
            return
        }

        // The gateway owns the single welcome message. Providers must never add one,
        // otherwise a reconnect can produce repeated greetings.
    }

    private fun forwardFallbackFlows() {
        if (fallbackForwardingStarted) return
        fallbackForwardingStarted = true
        scope.launch {
            fallbackProvider.receiveTranscripts().collect {
                transcriptFlow.emit(it)
            }
        }
        scope.launch {
            fallbackProvider.receiveAudio().collect {
                audioFlow.emit(it)
            }
        }
    }

    private suspend fun activateFallback(sessionId: String, config: ProviderConfig) {
        if (!isUsingFallback) {
            isUsingFallback = true
            fallbackProvider.connect(sessionId, config)
        }
        forwardFallbackFlows()
    }

    override suspend fun sendAudio(audioBytes: ByteArray) {
        // Audio stays on-device by default. Android's recognizer handles the live
        // transcript, which avoids expensive multimodal/audio calls for each turn.
        if (!isConnected) return
        fallbackProvider.sendAudio(audioBytes)
    }

    override suspend fun sendText(text: String) {
        if (!isConnected) return
        // A stop button cancels only the current turn; it must never mute the rest of a session.
        isInterrupted = false
        transcriptFlow.emit(TranscriptChunk(text, isUser = true, isFinal = true))

        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "YOUR_OPENROUTER_API_KEY") {
            fallbackProvider.sendText(text)
            return
        }

        scope.launch {
            executeGeminiRequest(audioBase64 = null, userText = text)
        }
    }

    private suspend fun executeGeminiRequest(audioBase64: String?, userText: String?) = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        val config = currentConfig ?: return@withContext

        try {
            val userPart = JSONObject()
            if (userText != null) {
                userPart.put("text", userText)
            } else if (audioBase64 != null) {
                val inlineData = JSONObject().apply {
                    put("mimeType", "audio/wav")
                    put("data", audioBase64)
                }
                userPart.put("inlineData", inlineData)
            }

            val userContent = JSONObject().apply {
                put("role", "user")
                put("content", if (userText != null) userText else userPart.toString())
            }

            conversationHistory.add(userContent)
            // Trim window: keep last 6 turns (Level 2 memory)
            while (conversationHistory.size > 6) {
                conversationHistory.removeAt(0)
            }

            val messagesArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", config.systemPrompt)
                })
                conversationHistory.forEach { put(it) }
            }

            val requestJson = JSONObject().apply {
                put("model", getModel())
                put("messages", messagesArray)
                put("temperature", config.temperature)
                put("max_tokens", 110)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestJson.toString().toRequestBody(mediaType)
            val url = "https://openrouter.ai/api/v1/chat/completions"

            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer $apiKey")
                .header("HTTP-Referer", "https://linguabloom.app")
                .header("X-OpenRouter-Title", "LinguaBloom Nyra")
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                Log.w("OpenRouterVoice", "OpenRouter HTTP error ${response.code}: $errorBody")
                // Fallback to local
                activateFallback("fallback-$activeSessionId", config)
                userText?.let { fallbackProvider.sendText(it) }
                return@withContext
            }

            val responseBodyString = response.body?.string() ?: return@withContext
            val responseJson = JSONObject(responseBodyString)
            val choices = responseJson.optJSONArray("choices")
            val textReply = choices?.optJSONObject(0)
                ?.optJSONObject("message")
                ?.optString("content", "")
                ?.trim()
                .orEmpty()

            if (textReply.isNotBlank() && !isInterrupted) {
                val assistantContent = JSONObject().apply {
                    put("role", "assistant")
                    put("content", textReply)
                }
                conversationHistory.add(assistantContent)

                transcriptFlow.emit(TranscriptChunk(textReply, isUser = false, isFinal = true))
                ttsManager?.speak(textReply, config.targetLanguage)
            }
        } catch (e: Exception) {
            Log.e("OpenRouterVoice", "Request failed: ${e.message}, falling back to local provider")
            activateFallback("fallback-$activeSessionId", config)
            if (userText != null) {
                fallbackProvider.sendText(userText)
            }
        }
    }

    private suspend fun generateAndSpeak(prompt: String, isSystemGreeting: Boolean = false) {
        // The Gemini API requires a non-empty content part. Supplying null for the greeting
        // produced an empty request and left the UI waiting on some devices.
        executeGeminiRequest(audioBase64 = null, userText = prompt)
    }

    private fun getApiKey(): String {
        return try {
            BuildConfig::class.java.getField("OPENROUTER_API_KEY").get(null) as? String ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun getModel(): String = try {
        (BuildConfig::class.java.getField("OPENROUTER_MODEL").get(null) as? String)
            ?.takeIf { it.isNotBlank() } ?: "openrouter/free"
    } catch (_: Exception) { "openrouter/free" }

    override suspend fun interrupt() {
        isInterrupted = true
        ttsManager?.stop()
        fallbackProvider.interrupt()
    }

    override fun receiveAudio(): Flow<AudioChunk> = audioFlow.asSharedFlow()

    override fun receiveTranscripts(): Flow<TranscriptChunk> = transcriptFlow.asSharedFlow()

    override suspend fun close() {
        isConnected = false
        isInterrupted = true
        ttsManager?.stop()
        fallbackProvider.close()
    }
}
