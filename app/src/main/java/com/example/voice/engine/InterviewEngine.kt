package com.example.voice.engine

import com.example.voice.model.InterviewType

data class InterviewState(
    val interviewType: InterviewType,
    val targetRole: String,
    val difficulty: String = "Medium",
    var currentQuestionIndex: Int = 0,
    val questions: MutableList<String> = mutableListOf(),
    val answers: MutableList<String> = mutableListOf(),
    val followups: MutableList<String> = mutableListOf()
)

class InterviewEngine {

    fun initializeInterview(type: InterviewType, targetRole: String = "Software Engineer"): InterviewState {
        val questions = when (type) {
            InterviewType.HR -> mutableListOf(
                "Welcome to the interview! Could you please introduce yourself and walk me through your background?",
                "What motivated you to apply for this role, and why are you interested in our company?",
                "Where do you see yourself professionally in the next three to five years?",
                "What kind of work culture or environment allows you to do your best work?"
            )
            InterviewType.BEHAVIORAL -> mutableListOf(
                "Tell me about a time when you faced a difficult conflict within your team and how you resolved it.",
                "Describe a situation where a project missed a deadline or failed. What did you learn from it?",
                "Give an example of a goal you set and how you measured your progress along the way.",
                "Can you share an experience where you had to adapt quickly to an unexpected change in priorities?"
            )
            InterviewType.TECHNICAL -> mutableListOf(
                "Can you walk me through an end-to-end technical system or application you designed and built?",
                "How do you approach debugging and optimizing performance when an application is slow or unresponsive?",
                "Explain the tradeoffs between local offline caching and real-time remote cloud sync in modern apps.",
                "How do you ensure automated test coverage and reliability before deploying code to production?"
            )
            InterviewType.INTERNSHIP -> mutableListOf(
                "Tell me about your favorite academic or personal project. What was your specific contribution?",
                "How do you prioritize your time when balancing multiple coursework assignments and extracurriculars?",
                "What technical skills or tools are you most eager to learn during this internship?",
                "Describe a concept you found challenging to learn at first and how you mastered it."
            )
            InterviewType.FRESHER -> mutableListOf(
                "Tell me about your educational background and the core subjects that excited you most.",
                "What practical experience do you have with collaborative team projects or open-source software?",
                "How do you handle receiving constructive feedback or criticism on your code?",
                "Why should our company hire you for this entry-level opportunity?"
            )
        }

        return InterviewState(
            interviewType = type,
            targetRole = targetRole,
            currentQuestionIndex = 0,
            questions = questions
        )
    }

    /**
     * Gets the next question adaptively.
     * If the user gave a very short answer, asks a clarifying followup.
     */
    fun getNextQuestion(state: InterviewState, lastAnswer: String?): String? {
        if (lastAnswer != null) {
            state.answers.add(lastAnswer)
            // Check if answer was brief (under 10 words) -> add an adaptive followup
            val wordCount = lastAnswer.trim().split(Regex("\\s+")).size
            if (wordCount < 10 && state.followups.isEmpty()) {
                val followup = "Could you elaborate a bit more on that? Specifically, what was the impact or outcome?"
                state.followups.add(followup)
                return followup
            }
        }

        if (state.currentQuestionIndex < state.questions.size) {
            val q = state.questions[state.currentQuestionIndex]
            state.currentQuestionIndex++
            return q
        }

        return null // Interview finished
    }
}
