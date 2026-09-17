package com.example.voice.model

enum class VoiceSessionState {
    IDLE,
    CONNECTING,
    LISTENING,
    PROCESSING,
    ASSISTANT_SPEAKING,
    INTERRUPTED,
    ERROR,
    ENDED
}

enum class VoiceMode(val displayName: String, val description: String) {
    FRIEND("Friend Mode", "Casual chat, friendly encouragement, minimal interruptions"),
    TUTOR("Tutor Mode", "Active teaching, grammar hints, vocabulary explanations & practice"),
    SPEAKING_PRACTICE("Speaking Practice", "Topic-driven fluency drill with comprehensive end evaluation"),
    INTERVIEW("Interview Mode", "Professional multi-round interview with structured rubric feedback"),
    ROLEPLAY("Roleplay Mode", "Realistic scenarios like ordering at a cafe or checking in at an airport")
}

enum class InterviewType(val title: String, val role: String) {
    HR("HR Screening", "HR Specialist"),
    BEHAVIORAL("Behavioral (STAR)", "Hiring Manager"),
    TECHNICAL("Technical Discussion", "Lead Engineer"),
    INTERNSHIP("Internship Interview", "University Recruiter"),
    FRESHER("Fresher Entry-Level", "Talent Partner")
}

enum class RoleplayScenario(val title: String, val setting: String, val partnerRole: String) {
    RESTAURANT("Restaurant", "Ordering food, asking for substitutions & the bill", "Friendly Waiter"),
    AIRPORT("Airport", "Checking baggage, gate inquiry & immigration", "Flight Agent"),
    CUSTOMER_SUPPORT("Customer Support", "Returning a defective order or seeking a refund", "Support Rep"),
    JOB_INTERVIEW("Job Interview", "Short workplace situational chat", "Interviewer"),
    COLLEGE_DISCUSSION("College Discussion", "Debating a campus study project with a peer", "Classmate"),
    MEETING("Workplace Meeting", "Presenting a quick project update to the team", "Colleague"),
    NETWORKING("Networking Event", "Introducing yourself and exchanging contact details", "Fellow Professional"),
    TRAVEL("Travel & Hotel", "Booking a tour and asking for local recommendations", "Hotel Concierge"),
    SHOPPING("Boutique Shopping", "Asking for sizes, discounts & payment methods", "Store Clerk"),
    WORKPLACE_CONVERSATION("Coffee Break", "Casual workplace banter by the water cooler", "Office Friend")
}
