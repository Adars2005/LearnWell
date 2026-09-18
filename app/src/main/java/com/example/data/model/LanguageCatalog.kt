package com.example.data.model

/** One source of truth for every selectable learning language and Android locale. */
object LanguageCatalog {
    val supported = listOf(
        "Hindi", "Spanish", "French", "Japanese", "German", "Italian", "Korean", "Mandarin",
        "Portuguese", "Arabic", "Russian", "Turkish", "English", "Bengali", "Tamil", "Telugu",
        "Marathi", "Gujarati", "Punjabi", "Vietnamese", "Indonesian", "Dutch", "Thai"
    )

    fun localeTag(language: String): String = when (language.lowercase()) {
        "hindi" -> "hi-IN"; "spanish" -> "es-ES"; "french" -> "fr-FR"; "japanese" -> "ja-JP"
        "german" -> "de-DE"; "italian" -> "it-IT"; "korean" -> "ko-KR"; "mandarin" -> "zh-CN"
        "portuguese" -> "pt-BR"; "arabic" -> "ar-SA"; "russian" -> "ru-RU"; "turkish" -> "tr-TR"
        "bengali" -> "bn-IN"; "tamil" -> "ta-IN"; "telugu" -> "te-IN"; "marathi" -> "mr-IN"
        "gujarati" -> "gu-IN"; "punjabi" -> "pa-IN"; "vietnamese" -> "vi-VN"; "indonesian" -> "id-ID"
        "dutch" -> "nl-NL"; "thai" -> "th-TH"; else -> "en-US"
    }
}
