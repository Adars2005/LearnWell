package com.example.voice.provider

import kotlinx.coroutines.flow.Flow

data class ProviderConfig(
    val model: String = "gemini-2.5-flash-native-audio-preview-12-2025",
    val systemPrompt: String,
    val targetLanguage: String,
    val learnerLevel: String,
    val temperature: Float = 0.7f
)

data class AudioChunk(
    val data: ByteArray,
    val sampleRate: Int = 24000,
    val isFinal: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AudioChunk
        return data.contentEquals(other.data) && sampleRate == other.sampleRate && isFinal == other.isFinal
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + sampleRate
        result = 31 * result + isFinal.hashCode()
        return result
    }
}

data class TranscriptChunk(
    val text: String,
    val isUser: Boolean,
    val isFinal: Boolean
)

interface VoiceProvider {
    val providerName: String

    suspend fun connect(sessionId: String, config: ProviderConfig)
    suspend fun sendAudio(audioBytes: ByteArray)
    suspend fun sendText(text: String)
    suspend fun interrupt()
    fun receiveAudio(): Flow<AudioChunk>
    fun receiveTranscripts(): Flow<TranscriptChunk>
    suspend fun close()
}
