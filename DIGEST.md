# LinguaBloom Codebase Digest & Technical Architecture

**Application Name:** LinguaBloom  
**Package:** `com.example` (Application ID: `com.aistudio.vocablingo.rkwvbx`)  
**Target Platform:** Android (Min SDK 26, Target SDK 34)  
**Primary Tech Stack:** Kotlin, Jetpack Compose, Room Database, Kotlin Coroutines & StateFlow, Android TTS (Text-to-Speech), Material 3.

---

## 1. Project Overview & Philosophy

LinguaBloom is a language learning Android application structured around scientific Spaced Repetition (SuperMemo-2 algorithm) and mentor-guided instruction featuring **Teacher Maya**. It supports multiple languages (Hindi, Spanish, French, Japanese) with:
- **Memory Garden Visualizer:** Gamified retention tracking through 4 stages: Seed 🌱, Sprout 🌿, Flower 🌸, and Full Bloom 🌺.
- **Active Spaced Repetition Engine (SRS):** Calculates optimal review intervals based on user grading (quality 0 to 5), ease factor adjustment, and forgetting curve retention probability.
- **Interactive Dual-Mode Practice:**
  - Audio Listening & Construction (normal & slow speed TTS playback).
  - Sentence Translation using an interactive word bank and double-underline target slots.
- **Zero-Penalty Mistake Rehabilitation:** Dedicated flow allowing learners to review and fix mistakes without expending hearts/energy.
- **Interactive Memory Vault & Active Flashcards:** Full dictionary browser with retention meters, flip cards, and category filtering.

---

## 2. Directory Tree

```
/
├── metadata.json
├── settings.gradle.kts
├── build.gradle.kts
└── app/
    ├── build.gradle.kts
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── java/com/example/
        │   │   ├── MainActivity.kt
        │   │   ├── data/
        │   │   │   ├── local/
        │   │   │   │   ├── AppDatabase.kt
        │   │   │   │   ├── InitialVocabData.kt
        │   │   │   │   └── VocabDao.kt
        │   │   │   ├── model/
        │   │   │   │   ├── UserProfile.kt
        │   │   │   │   └── VocabWord.kt
        │   │   │   ├── repository/
        │   │   │   │   └── VocabRepository.kt
        │   │   │   └── srs/
        │   │   │       └── SpacedRepetitionEngine.kt
        │   │   ├── ui/
        │   │   │   ├── components/
        │   │   │   │   ├── DuoButton.kt
        │   │   │   │   ├── DuoProgressBar.kt
        │   │   │   │   ├── DuoTopBar.kt
        │   │   │   │   ├── FeedbackBottomSheet.kt
        │   │   │   │   ├── MascotSpeechBubble.kt
        │   │   │   │   └── WordChip.kt
        │   │   │   ├── screens/
        │   │   │   │   ├── GoalSelectScreen.kt
        │   │   │   │   ├── HomeDashboardScreen.kt
        │   │   │   │   ├── MistakeReviewIntroScreen.kt
        │   │   │   │   ├── PracticeExerciseScreen.kt
        │   │   │   │   ├── SessionCelebrationScreen.kt
        │   │   │   │   ├── StartingPointScreen.kt
        │   │   │   │   ├── VocabVaultScreen.kt
        │   │   │   │   └── WelcomeScreen.kt
        │   │   │   └── theme/
        │   │   │       ├── Color.kt
        │   │   │       ├── Theme.kt
        │   │   │       └── Type.kt
        │   │   ├── util/
        │   │   │   └── TtsManager.kt
        │   │   └── viewmodel/
        │   │       └── MainViewModel.kt
        │   └── res/
        │       ├── drawable/ (teacher avatars, icons, vectors)
        │       └── values/strings.xml
        └── test/
            └── java/com/example/
                ├── ExampleRobolectricTest.kt
                ├── ExampleUnitTest.kt
                ├── GreetingScreenshotTest.kt
                └── SpacedRepetitionEngineTest.kt
```

---

## 3. Module & File Digest

### 3.1 Data Layer (`com.example.data`)

#### Models (`com.example.data.model`)
- **`VocabWord.kt`**:
  - Room entity (`vocab_words` table).
  - Fields: `id`, `language`, `word`, `phonetic`, `translation`, `category`, `exampleSentence`, `exampleTranslation`, `audioPrompt`, `teacherTip`.
  - SRS state: `easeFactor` (default 2.5), `intervalDays`, `repetitions`, `nextReviewTimestamp`, `lastReviewedTimestamp`, `mistakeCount`, `isMistake`, `totalReviews`, `consecutiveCorrect`.
  - Helpers: `bloomStage` ("Seed", "Sprout", "Flower", "Full Bloom"), `bloomIcon` ("🌱", "🌿", "🌸", "🌺"), `retentionPercentage` ($R = e^{-t/S}$ forgetting curve model).
- **`UserProfile.kt`**:
  - Room entity (`user_profiles` table).
  - Tracks `targetLanguage`, `streakDays`, `hearts` (energy), `gems`, `xp`, `dailyGoalWords`, `todayWordsPracticed`, `lastActiveDate`, `hasCompletedOnboarding`, `soundEnabled`.

#### Database & Persistence (`com.example.data.local`)
- **`AppDatabase.kt`**:
  - Room Database (`linguabloom_database`, version 2, fallback destructive migration).
  - Pre-populates words asynchronously via `RoomDatabase.Callback.onCreate` using `InitialVocabData`.
- **`VocabDao.kt`**:
  - Reactive queries using `Flow<List<VocabWord>>` and `Flow<UserProfile?>`.
  - Targeted queries: `getWordsDueForReview(language, currentTime)`, `getMistakeWords(language)`, `getWordsByLanguage(language)`, `getNewWords(language, limit)`.
  - Profile queries: `updateHearts`, `incrementStreak`, `addXpAndGems`, `resetDailyProgress`.
- **`InitialVocabData.kt`**:
  - Curated initial decks for **Hindi** (देवनागरी phonetics, cultural tips on polite speech and gender), **Spanish**, **French**, and **Japanese** (Hiragana, phonetic guides, and cultural notes).

#### Spaced Repetition Engine (`com.example.data.srs`)
- **`SpacedRepetitionEngine.kt`**:
  - Pure algorithmic implementation of the SuperMemo-2 (SM-2) algorithm.
  - Quality ratings (0 to 5).
  - Formulas:
    - $EF' = EF + (0.1 - (5 - q) \times (0.08 + (5 - q) \times 0.02))$ clamped at minimum 1.3.
    - Interval progression: $I(1) = 1$ day, $I(2) = 6$ days, $I(n) = I(n-1) \times EF$.
    - Lapses ($q < 3$): resets repetitions to 0, sets interval to 1 day, marks `isMistake = true`.

#### Repository (`com.example.data.repository`)
- **`VocabRepository.kt`**:
  - Abstraction separating ViewModel from Room DAO.
  - Exposes Flows for due words, mistake words, full library, and user profile.
  - Encapsulates `processReview(wordId, quality)` executing the SRS calculations inside database transactions.

---

### 3.2 Utilities (`com.example.util`)
- **`TtsManager.kt`**:
  - Encapsulates Android `TextToSpeech` engine.
  - Language locale mapper: Hindi (`hi_IN`), Spanish (`es_ES`), French (`fr_FR`), Japanese (`ja_JP`).
  - Supports dual playback rates: `speakNormal(text)` (1.0f rate) and `speakSlow(text)` (0.6f rate for phonetics listening).
  - Handles lifecycle initialization and cleanup via `shutdown()`.

---

### 3.3 State Management & ViewModel (`com.example.viewmodel`)
- **`MainViewModel.kt`**:
  - Central state orchestrator for all screens.
  - Screen routing enum `Screen`: `WELCOME`, `GOAL_SELECT`, `STARTING_POINT`, `HOME`, `PRACTICE`, `MISTAKES_REVIEW_INTRO`, `MISTAKES_PRACTICE`, `SESSION_CELEBRATION`, `VOCAB_VAULT`.
  - Practice state machine:
    - Current exercise index, active token bank, selected sentence slots, feedback state (`IDLE`, `CORRECT`, `INCORRECT`), celebration summary data.
  - Heart & Streak economy: Deducts hearts on mistake during regular sessions, preserves hearts during mistake reviews, awards XP and gems on session completion.

---

### 3.4 Presentation & UI (`com.example.ui`)

#### Design System & Theme (`com.example.ui.theme`)
- **`Color.kt`**: LinguaBloom signature warm palette (`BloomCoral`, `BloomEmerald`, `BloomViolet`, `BloomAmber`, `BloomPeach`, `BloomBackground`, `BloomSurface`).
- **`Type.kt`**: Rounded typography configuration using Material 3 standard sizing.
- **`Theme.kt`**: `LinguaBloomTheme` implementing light color scheme and edge-to-edge support.

#### Custom Components (`com.example.ui.components`)
- **`DuoButton.kt`**: Duolingo-styled 3D tactile button with layered shadow press effect (`PRIMARY`, `SECONDARY`, `OUTLINED`, `CORRECT`, `ERROR`).
- **`DuoProgressBar.kt`**: Rounded pill progress bar with smooth spring animation.
- **`DuoTopBar.kt`**: Two headers: `DuoPracticeTopBar` (with exit cross, progress bar, animated heart counter) and `DuoHomeTopBar` (language selector flag, streak flame, gem counter, heart indicator).
- **`FeedbackBottomSheet.kt`**: Sliding bottom feedback panel with checkmark/cross banner, target translation, phonetics, audio replay, and Teacher Maya's mnemonic tip.
- **`MascotSpeechBubble.kt`**: Circular mentor avatar (Teacher Maya) paired with responsive speech bubble, audio pronounce trigger, and tip pill.
- **`WordChip.kt`**: Interactive tactile word tokens for sentence building with used/unused states.

#### Screens (`com.example.ui.screens`)
1. **`WelcomeScreen.kt`**: Engaging intro with orbital hero avatar of Teacher Maya, brand tag, core feature pills, and onboarding buttons.
2. **`GoalSelectScreen.kt`**: Step 1 onboarding selecting daily pacing (Casual 5 min, Regular 10 min, Serious 15 min, Intense 20 min).
3. **`StartingPointScreen.kt`**: Step 2 onboarding selecting learner baseline (First time vs. Previous knowledge test).
4. **`HomeDashboardScreen.kt`**: Central hub featuring Teacher Maya's greeting, SRS Daily Deck hero card, 4-stage Memory Garden widget, Daily Goal bar, Quick-action cards (Fix Mistakes, Learn New Words, Memory Vault), and Cultural Spotlight of the Day.
5. **`PracticeExerciseScreen.kt`**: Active learning canvas supporting dual exercise types (Listening Drill with normal/slow TTS & Translation sentence building), word bank slots, and feedback action footer.
6. **`MistakeReviewIntroScreen.kt`**: Zero-energy encouraging screen prior to mistake review.
7. **`SessionCelebrationScreen.kt`**: Win screen featuring Teacher Maya with gold medal, stats breakdown (accuracy, XP earned, gems rewarded), and streak celebration.
8. **`VocabVaultScreen.kt`**: Dictionary and interactive flashcard viewer with category filter, search bar, and memory retention bar per item.

---

## 4. Test Suite (`app/src/test`)
- **`SpacedRepetitionEngineTest.kt`**: Unit tests verifying SM-2 formulas, repetition increments, interval calculations, and lapse resets.
- **`ExampleRobolectricTest.kt`**: Robolectric JVM unit test verifying application context, string resources, and app identity.
- **`ExampleUnitTest.kt`**: Base JVM assertions.
- **`GreetingScreenshotTest.kt`**: Roborazzi screenshot test harness for Compose UI validation.

---

## 5. Verification & Compilation Status
- Built and verified with Gradle Kotlin DSL (`compile_applet` passed).
- Unit and Robolectric test suite execution verified.
- Platform metadata (`metadata.json`), Android manifest, and app name resources are fully synchronized under **LinguaBloom**.
