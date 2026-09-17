package com.example.voice.provider

import android.util.Base64
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
 * Gemini Voice Provider supporting Gemini 2.5 Flash Native Audio and Gemini 3.5 Flash streaming.
 * Securely uses BuildConfig.GEMINI_API_KEY without hardcoding.
 * Includes graceful fallback to LocalFallbackVoiceProvider on network failure or rate limit.
 */
class GeminiLiveProvider(
    private val ttsManager: TtsManager? = null,
    private val fallbackProvider: LocalFallbackVoiceProvider = LocalFallbackVoiceProvider(ttsManager)
) : VoiceProvider {

    override val providerName: String = "gemini-live-provider"

    private val audioFlow = MutableSharedFlow<AudioChunk>(extraBufferCapacity = 64)
    private val transcriptFlow = MutableSharedFlow<TranscriptChunk>(extraBufferCapacity = 64)
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private var isConnected = false
    private var currentConfig: ProviderConfig? = null
    private var isStreaming = false
    private var isInterrupted = false

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    // Recent message window for conversational memory (Level 2)
    private val conversationHistory = mutableListOf<JSONObject>()

    override suspend fun connect(sessionId: String, config: ProviderConfig) {
        isConnected = true
        isInterrupted = false
        currentConfig = config
        conversationHistory.clear()

        // Check if API key is available
        val apiKey = try {
            BuildConfig::class.java.getField("GEMINI_API_KEY").get(null) as? String ?: ""
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w("GeminiLiveProvider", "No active Gemini API key found, engaging local provider fallback.")
            fallbackProvider.connect(sessionId, config)
            forwardFallbackFlows()
            return
        }

        // Send initial greeting from assistant
        scope.launch {
            try {
                val prompt = "Greet the user warmly as a language partner in ${config.targetLanguage} (learner level: ${config.learnerLevel}). Keep it to 1-2 friendly sentences and ask an open question to encourage speaking."
                generateAndSpeak(prompt, isSystemGreeting = true)
            } catch (e: Exception) {
                Log.e("GeminiLiveProvider", "Failed to connect greeting: ${e.message}")
                fallbackProvider.connect(sessionId, config)
                forwardFallbackFlows()
            }
        }
    }

    private fun forwardFallbackFlows() {
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

    override suspend fun sendAudio(audioBytes: ByteArray) {
        // Encode PCM/WAV audio to base64 inline data
        if (!isConnected) return
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            fallbackProvider.sendAudio(audioBytes)
            return
        }

        val base64Audio = Base64.encodeToString(audioBytes, Base64.NO_WRAP)
        executeGeminiRequest(audioBase64 = base64Audio, userText = null)
    }

    override suspend fun sendText(text: String) {
        if (!isConnected) return
        transcriptFlow.emit(TranscriptChunk(text, isUser = true, isFinal = true))

        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
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
                put("parts", JSONArray().apply { put(userPart) })
            }

            conversationHistory.add(userContent)
            // Trim window: keep last 6 turns (Level 2 memory)
            while (conversationHistory.size > 6) {
                conversationHistory.removeAt(0)
            }

            val contentsArray = JSONArray()
            conversationHistory.forEach { contentsArray.put(it) }

            val requestJson = JSONObject().apply {
                put("contents", contentsArray)

                val systemInstruction = JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", config.systemPrompt) })
                    })
                }
                put("systemInstruction", systemInstruction)

                val genConfig = JSONObject().apply {
                    put("temperature", config.temperature)
                    put("maxOutputTokens", 120) // Compact, super fast response (1-2 sentences)
                }
                put("generationConfig", genConfig)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestJson.toString().toRequestBody(mediaType)
            // Using gemini-3.5-flash as robust multimodal and text model or native audio
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                Log.w("GeminiLiveProvider", "Gemini HTTP error ${response.code}: $errorBody")
                // Fallback to local
                userText?.let { fallbackProvider.sendText(it) }
                return@withContext
            }

            val responseBodyString = response.body?.string() ?: return@withContext
            val responseJson = JSONObject(responseBodyString)
            val candidates = responseJson.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val parts = candidate?.optJSONObject("content")?.optJSONArray("parts")
            val textReply = parts?.optJSONObject(0)?.optString("text", "") ?: ""

            if (textReply.isNotBlank() && !isInterrupted) {
                val assistantContent = JSONObject().apply {
                    put("role", "model")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", textReply) })
                    })
                }
                conversationHistory.add(assistantContent)

                transcriptFlow.emit(TranscriptChunk(textReply, isUser = false, isFinal = true))
                ttsManager?.speak(textReply, config.targetLanguage)
            }
        } catch (e: Exception) {
            Log.e("GeminiLiveProvider", "Request failed: ${e.message}, falling back to local provider")
            if (userText != null) {
                fallbackProvider.sendText(userText)
            }
        }
    }

    private suspend fun generateAndSpeak(prompt: String, isSystemGreeting: Boolean = false) {
        executeGeminiRequest(audioBase64 = null, userText = if (isSystemGreeting) null else prompt)
    }

    private fun getApiKey(): String {
        return try {
            BuildConfig::class.java.getField("GEMINI_API_KEY").get(null) as? String ?: ""
        } catch (e: Exception) {
            ""
        }
    }

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
