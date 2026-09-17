package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.MascotSpeechBubble
import com.example.ui.theme.BloomAmber
import com.example.ui.theme.BloomCoral
import com.example.ui.theme.BloomEmerald
import com.example.ui.theme.BloomPeach

data class PlacementQuestion(
    val tier: Int, // 1: A1, 2: A2, 3: B1
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int
)

@Composable
fun StartingPointScreen(
    language: String,
    onStartSelected: (level: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var questionNumber by remember { mutableIntStateOf(1) }
    var currentTier by remember { mutableIntStateOf(1) } // 1: A1, 2: A2, 3: B1
    var correctCount by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var hasAnsweredCurrent by remember { mutableStateOf(false) }
    var isFinished by remember { mutableStateOf(false) }

    val questionBank = remember(language) { getPlacementBank(language) }

    // Pick question from the current tier
    val currentQuestion = remember(questionNumber, currentTier) {
        val tierPool = questionBank.filter { it.tier == currentTier }
        if (tierPool.isNotEmpty()) {
            tierPool[(questionNumber - 1) % tierPool.size]
        } else {
            questionBank[(questionNumber - 1) % questionBank.size]
        }
    }

    val evaluatedLevel = remember(correctCount, currentTier) {
        when {
            currentTier >= 3 && correctCount >= 7 -> "B1"
            correctCount >= 4 -> "A2"
            else -> "A1"
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFFBF9))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        if (!isFinished) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                // Top Navigation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF64748B)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    LinearProgressIndicator(
                        progress = { questionNumber.toFloat() / 10f },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = BloomCoral,
                        trackColor = Color(0xFFE2E8F0)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "$questionNumber/10",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                }

                MascotSpeechBubble(
                    imageRes = R.drawable.img_teacher_avatar,
                    text = "Question $questionNumber of 10 • Let's adaptively test your $language skills!",
                    avatarSize = 65.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tier indicator badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            when (currentTier) {
                                1 -> Color(0xFFE0F2FE)
                                2 -> Color(0xFFFEF3C7)
                                else -> Color(0xFFFCE7F3)
                            }
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when (currentTier) {
                            1 -> "Tier: A1 (Foundations)"
                            2 -> "Tier: A2 (Elementary)"
                            else -> "Tier: B1 (Intermediate)"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (currentTier) {
                            1 -> Color(0xFF0369A1)
                            2 -> Color(0xFFB45309)
                            else -> Color(0xFFBE185D)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Question Prompt
                Text(
                    text = currentQuestion.prompt,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E293B)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Options
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    currentQuestion.options.forEachIndexed { index, option ->
                        val isSelected = selectedOption == index
                        val isCorrect = index == currentQuestion.correctIndex

                        val bgColor = when {
                            hasAnsweredCurrent && isCorrect -> Color(0xFFECFDF5)
                            hasAnsweredCurrent && isSelected && !isCorrect -> Color(0xFFFEF2F2)
                            isSelected -> BloomPeach
                            else -> Color.White
                        }
                        val borderColor = when {
                            hasAnsweredCurrent && isCorrect -> BloomEmerald
                            hasAnsweredCurrent && isSelected && !isCorrect -> Color(0xFFEF4444)
                            isSelected -> BloomCoral
                            else -> Color(0xFFE2E8F0)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .border(2.dp, borderColor, RoundedCornerShape(14.dp))
                                .background(bgColor)
                                .clickable(enabled = !hasAnsweredCurrent) {
                                    selectedOption = index
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = option,
                                fontSize = 16.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = Color(0xFF1E293B)
                            )
                            if (hasAnsweredCurrent) {
                                if (isCorrect) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Correct",
                                        tint = BloomEmerald
                                    )
                                } else if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Wrong",
                                        tint = Color(0xFFEF4444)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Bottom Action Button
                if (!hasAnsweredCurrent) {
                    Button(
                        onClick = {
                            if (selectedOption != null) {
                                hasAnsweredCurrent = true
                                val correct = selectedOption == currentQuestion.correctIndex
                                if (correct) {
                                    correctCount++
                                    // Step up tier on correct
                                    if (currentTier < 3) currentTier++
                                } else {
                                    // Step down tier on wrong
                                    if (currentTier > 1) currentTier--
                                }
                            }
                        },
                        enabled = selectedOption != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BloomCoral)
                    ) {
                        Text("Check Answer", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            if (questionNumber < 10) {
                                questionNumber++
                                selectedOption = null
                                hasAnsweredCurrent = false
                            } else {
                                isFinished = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BloomEmerald)
                    ) {
                        Text(
                            text = if (questionNumber < 10) "Next Question" else "See My Placement Result 🎉",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                // Skip placement test option
                Text(
                    text = "Skip test and start at Beginner (A1)",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable { onStartSelected("A1") }
                        .padding(bottom = 12.dp)
                )
            }
        } else {
            // Evaluated Result Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "🎓", fontSize = 56.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Placement Complete!",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Based on your 10 adaptive questions, Teacher Maya has calibrated your curriculum.",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(BloomPeach)
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "RECOMMENDED CEFR LEVEL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = BloomCoral
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Level $evaluatedLevel",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (evaluatedLevel) {
                                "A1" -> "Fresh Seedling • Essential Vocabulary"
                                "A2" -> "Elementary Sprout • Daily Conversations"
                                else -> "Blooming Conversationalist • Idioms & Tenses"
                            },
                            fontSize = 13.sp,
                            color = Color(0xFF475569)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Score: $correctCount / 10 correct answers",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BloomEmerald
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { onStartSelected(evaluatedLevel) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BloomCoral)
                ) {
                    Text(
                        text = "Begin Blooming with Level $evaluatedLevel 🌸",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun getPlacementBank(language: String): List<PlacementQuestion> {
    return when (language.lowercase()) {
        "spanish" -> listOf(
            PlacementQuestion(1, "What does 'Hola' mean?", listOf("Hello", "Goodbye", "Please", "Water"), 0),
            PlacementQuestion(1, "Which word means 'apple'?", listOf("Manzana", "Libro", "Gato", "Casa"), 0),
            PlacementQuestion(1, "Choose the word for 'book':", listOf("Libro", "Agua", "Pan", "Sol"), 0),
            PlacementQuestion(2, "Complete: 'Ella ____ una carta.'", listOf("escribe", "escriben", "escribir", "escribes"), 0),
            PlacementQuestion(2, "What is 'yesterday' in Spanish?", listOf("Ayer", "Hoy", "Mañana", "Siempre"), 0),
            PlacementQuestion(2, "Choose 'I want to drink water':", listOf("Quiero beber agua", "Bebo agua ayer", "Agua comer quiero", "Bebe agua quiero"), 0),
            PlacementQuestion(3, "Subjunctive: 'Espero que tú ____ pronto.'", listOf("vengas", "vienes", "venir", "vendrás"), 0),
            PlacementQuestion(3, "What does the idiom 'tomar el pelo' mean?", listOf("Pull someone's leg (tease)", "Cut someone's hair", "Wash your face", "Go bald"), 0),
            PlacementQuestion(3, "Choose the conditional: 'Yo ____ si tuviera tiempo.'", listOf("iría", "fui", "iba", "vaya"), 0),
            PlacementQuestion(3, "Translate: 'Although it rains, we will leave.'", listOf("Aunque llueva, saldremos", "Porque llueve, salimos", "Si llueve, salir", "Cuando llovió, salí"), 0)
        )
        "french" -> listOf(
            PlacementQuestion(1, "What does 'Bonjour' mean?", listOf("Hello / Good day", "Thank you", "Bread", "Water"), 0),
            PlacementQuestion(1, "Which word means 'cat'?", listOf("Chat", "Chien", "Livre", "Maison"), 0),
            PlacementQuestion(1, "Choose the word for 'thank you':", listOf("Merci", "S'il vous plaît", "Au revoir", "Oui"), 0),
            PlacementQuestion(2, "Complete: 'Nous ____ à Paris.'", listOf("habitons", "habite", "habiter", "habites"), 0),
            PlacementQuestion(2, "What is 'tomorrow' in French?", listOf("Demain", "Hier", "Maintenant", "Toujours"), 0),
            PlacementQuestion(2, "Choose 'I would like a croissant':", listOf("Je voudrais un croissant", "Je veux croissants hier", "Croissant mange moi", "Croissant bon est"), 0),
            PlacementQuestion(3, "Subjunctive: 'Il faut que tu ____.'", listOf("viennes", "viens", "venir", "venu"), 0),
            PlacementQuestion(3, "Idiom: 'Avoir le coup de foudre' means:", listOf("Love at first sight", "Fear of lightning", "Headache", "To be struck by thunder"), 0),
            PlacementQuestion(3, "Conditional: 'Si j'avais su, je ____ venu.'", listOf("serais", "étais", "suis", "serai"), 0),
            PlacementQuestion(3, "Translate: 'Before leaving, eat something.'", listOf("Avant de partir, mange", "Après partir, manger", "Pour partir, manger", "Quand parti, mangé"), 0)
        )
        "german" -> listOf(
            PlacementQuestion(1, "What does 'Guten Tag' mean?", listOf("Good day", "Good night", "Apple", "Goodbye"), 0),
            PlacementQuestion(1, "Which word means 'water'?", listOf("Wasser", "Brot", "Buch", "Katze"), 0),
            PlacementQuestion(1, "Choose 'thank you':", listOf("Danke", "Bitte", "Hallo", "Nein"), 0),
            PlacementQuestion(2, "Accusative: 'Ich habe ____ Apfel.'", listOf("einen", "ein", "einer", "einem"), 0),
            PlacementQuestion(2, "What is 'yesterday' in German?", listOf("Gestern", "Heute", "Morgen", "Immer"), 0),
            PlacementQuestion(2, "Choose 'Where is the train station?':", listOf("Wo ist der Bahnhof?", "Wer ist Bahnhof?", "Warum Bahnhof ist?", "Wie geht Bahnhof?"), 0),
            PlacementQuestion(3, "Konjunktiv II: 'Wenn ich Zeit hätte, ____ ich kommen.'", listOf("würde", "werde", "habe", "hatte"), 0),
            PlacementQuestion(3, "Idiom: 'Die Daumen drücken' means:", listOf("Keep fingers crossed", "Press thumbs hard", "Hitchhike", "Give up"), 0),
            PlacementQuestion(3, "Dative plural: 'mit den ____'", listOf("Kindern", "Kinder", "Kindes", "Kinde"), 0),
            PlacementQuestion(3, "Relative clause: 'Das Buch, ____ ich lese.'", listOf("das", "den", "dem", "dessen"), 0)
        )
        "japanese" -> listOf(
            PlacementQuestion(1, "What is 'konnichiwa'?", listOf("Hello", "Thank you", "Water", "Cat"), 0),
            PlacementQuestion(1, "Which word means 'water'?", listOf("Mizu (水)", "Neko (猫)", "Hon (本)", "Inu (犬)"), 0),
            PlacementQuestion(1, "Choose 'thank you':", listOf("Arigatou", "Sayounara", "Kudasai", "Hai"), 0),
            PlacementQuestion(2, "Particle for direct object:", listOf("o (を)", "wa (は)", "ga (が)", "ni (に)"), 0),
            PlacementQuestion(2, "Past tense of 'tabemasu' (eat):", listOf("tabemashita", "tabemasen", "tabetai", "tabete"), 0),
            PlacementQuestion(2, "What does 'doko' mean?", listOf("Where", "When", "Who", "Why"), 0),
            PlacementQuestion(3, "Te-form request: 'Please speak':", listOf("Hanashite kudasai", "Hanasu kudasai", "Hanashitai kudasai", "Hanasenai"), 0),
            PlacementQuestion(3, "Idiom 'neko no te mo karitai' means:", listOf("Extremely busy", "Loving cats", "Having soft hands", "Cat scratching"), 0),
            PlacementQuestion(3, "Honorific prefix used for food/tea:", listOf("o- / go-", "sa-", "mi-", "yo-"), 0),
            PlacementQuestion(3, "Potential form of 'nomu' (drink):", listOf("nomeru", "nomitai", "nomou", "nomanai"), 0)
        )
        else -> listOf( // Hindi & default bank
            PlacementQuestion(1, "What does 'नमस्ते' (Namaste) mean?", listOf("Hello / Greetings", "Water", "Book", "Goodbye"), 0),
            PlacementQuestion(1, "Which word means 'book'?", listOf("किताब (Kitaab)", "पानी (Paani)", "चाय (Chaay)", "घर (Ghar)"), 0),
            PlacementQuestion(1, "Choose the word for 'water':", listOf("पानी (Paani)", "सेब (Seb)", "लड़का (Ladka)", "वह (Vah)"), 0),
            PlacementQuestion(2, "Translate: 'यह एक सेब है।' (Yah ek seb hai)", listOf("This is an apple", "That is a cat", "I eat an apple", "He has books"), 0),
            PlacementQuestion(2, "What is the feminine marker sound in Hindi?", listOf("Long 'i' (ी)", "Short 'u' (ु)", "Long 'aa' (ा)", "Nasal 'an'"), 0),
            PlacementQuestion(2, "Complete: 'लड़का किताब ____ है।' (The boy reads a book)", listOf("पढ़ता (padhta)", "पढ़ती (padhti)", "पढ़ते (padhte)", "पढ़ना (padhna)"), 0),
            PlacementQuestion(3, "Past tense: 'मैंने खाना ____।' (I ate food)", listOf("खाया (khaaya)", "खाता (khaata)", "खाऊँगा (khaoonga)", "खाना (khaana)"), 0),
            PlacementQuestion(3, "What does the expression 'चार चाँद लगना' mean?", listOf("To enhance charm/glory greatly", "To see the four moons", "To become dark", "To count stars"), 0),
            PlacementQuestion(3, "Choose the respectful imperative for 'come':", listOf("आइए (Aaiye)", "आओ (Aao)", "आ (Aa)", "आना (Aana)"), 0),
            PlacementQuestion(3, "Compound verb: 'वह सो ____।' (He fell asleep)", listOf("गया (gaya)", "किया (kiya)", "दिया (diya)", "लिया (liya)"), 0)
        )
    }
}
