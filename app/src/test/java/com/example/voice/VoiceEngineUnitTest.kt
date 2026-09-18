package com.example.voice

import com.example.voice.engine.GrammarCorrectionEngine
import com.example.voice.engine.InterviewEngine
import com.example.voice.engine.SpeakingEvaluationEngine
import com.example.voice.engine.VocabularyEngine
import com.example.voice.model.InterviewType
import com.example.voice.model.VoiceMode
import com.example.voice.orchestrator.AIOrchestrator
import com.example.voice.orchestrator.LocalAction
import com.example.voice.orchestrator.LocalCommandResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceEngineUnitTest {

    @Test
    fun grammarEngine_friendMode_doesNotInterruptMinorErrors() {
        val engine = GrammarCorrectionEngine()
        // Subject-verb agreement is not flagged in friend mode
        val correction = engine.analyzeGrammar("he don't like coffee", VoiceMode.FRIEND)
        assertNull(correction)
    }

    @Test
    fun grammarEngine_tutorMode_catchesSubjectVerbAgreement() {
        val engine = GrammarCorrectionEngine()
        val correction = engine.analyzeGrammar("he don't like coffee", VoiceMode.TUTOR)
        assertNotNull(correction)
        assertEquals("he doesn't like", correction?.correction)
    }

    @Test
    fun grammarEngine_interviewMode_neverInterrupts() {
        val engine = GrammarCorrectionEngine()
        val correction = engine.analyzeGrammar("I go yesterday and he don't like it", VoiceMode.INTERVIEW)
        assertNull(correction)
    }

    @Test
    fun orchestrator_deterministicCommandDetection() {
        val orchestrator = AIOrchestrator()
        val stopResult = orchestrator.checkLocalCommand("stop")
        assertTrue(stopResult is LocalCommandResult.Handled)
        assertEquals(LocalAction.STOP, (stopResult as LocalCommandResult.Handled).action)

        val repeatResult = orchestrator.checkLocalCommand("repeat please")
        assertTrue(repeatResult is LocalCommandResult.Handled)
        assertEquals(LocalAction.REPEAT, (repeatResult as LocalCommandResult.Handled).action)

        val nonCommand = orchestrator.checkLocalCommand("I enjoy learning new words")
        assertTrue(nonCommand is LocalCommandResult.NotACommand)
    }

    @Test
    fun interviewEngine_generatesSequentialQuestions() {
        val interviewEngine = InterviewEngine()
        val state = interviewEngine.initializeInterview(InterviewType.HR)
        assertEquals(0, state.currentQuestionIndex)

        val q1 = interviewEngine.getNextQuestion(state, null)
        assertNotNull(q1)
        assertEquals(1, state.currentQuestionIndex)

        val q2 = interviewEngine.getNextQuestion(state, "I have been working as a mobile developer for three years.")
        assertNotNull(q2)
        assertEquals(2, state.currentQuestionIndex)
    }

    @Test
    fun speakingEvaluationEngine_detectsFillerWordsAndScores() {
        val evaluationEngine = SpeakingEvaluationEngine()
        val transcripts = listOf(
            "Um, I think like, basically we should solve this problem.",
            "Actually, you know, it was a very interesting project."
        )
        val evaluation = evaluationEngine.evaluateSession(
            userTranscripts = transcripts,
            grammarErrors = emptyList(),
            targetLanguage = "English"
        )

        assertTrue(evaluation.fillerWords > 0)
        assertTrue(evaluation.overallScore in 50..100)
        assertTrue(evaluation.recommendations.isNotEmpty())
    }

    @Test
    fun vocabularyEngine_returnsCuratedWords() {
        val vocabEngine = VocabularyEngine()
        val words = vocabEngine.extractVocabulary("English", "We need to collaborate and negotiate.", "B1")
        assertTrue(words.isNotEmpty())
        assertTrue(words.size in 3..5)
    }
}
