package com.example.data.local

import com.example.data.model.VocabWord

object InitialVocabData {
    fun getInitialWords(): List<VocabWord> {
        val words = mutableListOf<VocabWord>()

        // HINDI VOCABULARY
        words.addAll(
            listOf(
                VocabWord(
                    language = "Hindi",
                    word = "एक सेब",
                    phonetic = "ek seb",
                    translation = "an apple",
                    category = "Food",
                    exampleSentence = "यह एक सेब है।",
                    exampleTranslation = "This is an apple.",
                    audioPrompt = "एक सेब",
                    teacherTip = "Maya's Tip: 'Seb' is pronounced with a soft 's'. Remember: 'Save an apple for Teacher Maya!' In Hindi, fruits are masculine nouns unless ending in 'i'."
                ),
                VocabWord(
                    language = "Hindi",
                    word = "किताब",
                    phonetic = "kitaab",
                    translation = "book",
                    category = "Basics",
                    exampleSentence = "वह एक किताब है।",
                    exampleTranslation = "That is a book.",
                    audioPrompt = "किताब",
                    teacherTip = "Maya's Tip: 'Kitaab' has a long 'aa' vowel sound. Books are considered revered symbols of the goddess Saraswati in Indian tradition."
                ),
                VocabWord(
                    language = "Hindi",
                    word = "वह",
                    phonetic = "vah",
                    translation = "that",
                    category = "Basics",
                    exampleSentence = "वह लड़का पढ़ता है।",
                    exampleTranslation = "That boy reads.",
                    audioPrompt = "वह",
                    teacherTip = "Maya's Tip: 'Vah' is used for distant objects or people ('that/he/she'). For nearby objects, we use 'yah' ('this')."
                ),
                VocabWord(
                    language = "Hindi",
                    word = "नमस्ते",
                    phonetic = "namaste",
                    translation = "hello",
                    category = "Greetings",
                    exampleSentence = "नमस्ते, आप कैसे हैं?",
                    exampleTranslation = "Hello, how are you?",
                    audioPrompt = "नमस्ते",
                    teacherTip = "Maya's Tip: 'Namaste' means 'I bow to the divine spark in you'. Accompany it with palms pressed together at chest level."
                ),
                VocabWord(
                    language = "Hindi",
                    word = "पानी",
                    phonetic = "paani",
                    translation = "water",
                    category = "Food",
                    exampleSentence = "मुझे ठंडा पानी चाहिए।",
                    exampleTranslation = "I want cold water.",
                    audioPrompt = "पानी",
                    teacherTip = "Maya's Tip: 'Paani' is an essential word when traveling. Always ask 'peene ka paani' for clean drinking water!"
                ),
                VocabWord(
                    language = "Hindi",
                    word = "चाय",
                    phonetic = "chaay",
                    translation = "tea",
                    category = "Food",
                    exampleSentence = "गरम चाय पियो।",
                    exampleTranslation = "Drink hot tea.",
                    audioPrompt = "चाय",
                    teacherTip = "Maya's Tip: 'Chaay' is brewed with cardamom, ginger, and milk across India. It connects friends and opens every warm conversation."
                ),
                VocabWord(
                    language = "Hindi",
                    word = "घर",
                    phonetic = "ghar",
                    translation = "house",
                    category = "Basics",
                    exampleSentence = "यह मेरा घर है।",
                    exampleTranslation = "This is my house.",
                    audioPrompt = "घर",
                    teacherTip = "Maya's Tip: The 'gh' sound is aspirated from deep in the throat. 'Ghar' implies not just physical walls, but warm family hearth."
                ),
                VocabWord(
                    language = "Hindi",
                    word = "लड़का",
                    phonetic = "ladka",
                    translation = "boy",
                    category = "People",
                    exampleSentence = "एक छोटा लड़का।",
                    exampleTranslation = "A little boy.",
                    audioPrompt = "लड़का",
                    teacherTip = "Maya's Tip: The 'd' in 'ladka' is a retroflex tap (tongue curled back to the roof of the mouth). Notice the masculine '-aa' ending."
                ),
                VocabWord(
                    language = "Hindi",
                    word = "लड़की",
                    phonetic = "ladki",
                    translation = "girl",
                    category = "People",
                    exampleSentence = "लड़की खुश है।",
                    exampleTranslation = "The girl is happy.",
                    audioPrompt = "लड़की",
                    teacherTip = "Maya's Tip: Contrasting with 'ladka', notice how changing the ending to '-ii' makes the noun feminine: 'ladki'."
                ),
                VocabWord(
                    language = "Hindi",
                    word = "धन्यवाद",
                    phonetic = "dhanyavaad",
                    translation = "thank you",
                    category = "Greetings",
                    exampleSentence = "आपकी मदद के लिए धन्यवाद।",
                    exampleTranslation = "Thank you for your help.",
                    audioPrompt = "धन्यवाद",
                    teacherTip = "Maya's Tip: A formal and deeply respectful way to say thank you. For casual friends, 'shukriya' is also very common."
                ),
                VocabWord(
                    language = "Hindi",
                    word = "बिल्ली",
                    phonetic = "billi",
                    translation = "cat",
                    category = "Animals",
                    exampleSentence = "बिल्ली दूध पीती है।",
                    exampleTranslation = "The cat drinks milk.",
                    audioPrompt = "बिल्ली",
                    teacherTip = "Maya's Tip: Notice the double 'll' (geminate consonant) — hold the 'l' slightly longer before releasing into the 'ii'."
                ),
                VocabWord(
                    language = "Hindi",
                    word = "दोस्त",
                    phonetic = "dost",
                    translation = "friend",
                    category = "People",
                    exampleSentence = "तुम मेरे सच्चे दोस्त हो।",
                    exampleTranslation = "You are my true friend.",
                    audioPrompt = "दोस्त",
                    teacherTip = "Maya's Tip: 'Dost' is gender-neutral and can refer to any dear companion. You can say 'dosti' for friendship!"
                )
            )
        )

        // SPANISH VOCABULARY
        words.addAll(
            listOf(
                VocabWord(
                    language = "Spanish",
                    word = "Una manzana",
                    phonetic = "oo-nah mahn-sah-nah",
                    translation = "an apple",
                    category = "Food",
                    exampleSentence = "Yo como una manzana.",
                    exampleTranslation = "I eat an apple.",
                    audioPrompt = "Una manzana",
                    teacherTip = "Maya's Tip: In Spanish, 'manzana' is feminine, so use the article 'una'. Fun fact: 'manzana' also means a city block!"
                ),
                VocabWord(
                    language = "Spanish",
                    word = "El libro",
                    phonetic = "el lee-broh",
                    translation = "the book",
                    category = "Basics",
                    exampleSentence = "Leo el libro todos los días.",
                    exampleTranslation = "I read the book every day.",
                    audioPrompt = "El libro",
                    teacherTip = "Maya's Tip: 'Libro' is masculine, taking 'el'. Connect it with the English word 'library' to remember easily!"
                ),
                VocabWord(
                    language = "Spanish",
                    word = "Hola",
                    phonetic = "oh-lah",
                    translation = "hello",
                    category = "Greetings",
                    exampleSentence = "¡Hola! ¿Cómo estás?",
                    exampleTranslation = "Hello! How are you?",
                    audioPrompt = "Hola",
                    teacherTip = "Maya's Tip: The letter 'H' is always silent in Spanish. Say 'OH-lah', never 'HO-lah'!"
                ),
                VocabWord(
                    language = "Spanish",
                    word = "Agua",
                    phonetic = "ah-gwah",
                    translation = "water",
                    category = "Food",
                    exampleSentence = "Por favor, un vaso de agua.",
                    exampleTranslation = "Please, a glass of water.",
                    audioPrompt = "Agua",
                    teacherTip = "Maya's Tip: 'Agua' is feminine, but because it begins with a stressed 'a', we say 'el agua' in singular to avoid awkward vowel clash."
                ),
                VocabWord(
                    language = "Spanish",
                    word = "Gracias",
                    phonetic = "grah-syahs",
                    translation = "thank you",
                    category = "Greetings",
                    exampleSentence = "Muchas gracias por tu ayuda.",
                    exampleTranslation = "Thank you very much for your help.",
                    audioPrompt = "Gracias",
                    teacherTip = "Maya's Tip: In Spain, the 'c' sounds like 'th' ('GRAH-thyahs'), while in Latin America it sounds like 's' ('GRAH-syahs'). Both are wonderful!"
                ),
                VocabWord(
                    language = "Spanish",
                    word = "Un gato",
                    phonetic = "oon gah-toh",
                    translation = "a cat",
                    category = "Animals",
                    exampleSentence = "El gato duerme aquí.",
                    exampleTranslation = "The cat sleeps here.",
                    audioPrompt = "Un gato",
                    teacherTip = "Maya's Tip: For a female cat, change the ending to 'una gata'. Notice how gendered endings harmonize naturally."
                ),
                VocabWord(
                    language = "Spanish",
                    word = "Buenos días",
                    phonetic = "bweh-nohs dee-ahs",
                    translation = "good morning",
                    category = "Greetings",
                    exampleSentence = "Buenos días a todos.",
                    exampleTranslation = "Good morning everyone.",
                    audioPrompt = "Buenos días",
                    teacherTip = "Maya's Tip: We use plural ('días') as a wish for good days in the plural! Used until lunchtime (around 1-2 PM)."
                ),
                VocabWord(
                    language = "Spanish",
                    word = "Amigo",
                    phonetic = "ah-mee-goh",
                    translation = "friend",
                    category = "People",
                    exampleSentence = "Él es mi mejor amigo.",
                    exampleTranslation = "He is my best friend.",
                    audioPrompt = "Amigo",
                    teacherTip = "Maya's Tip: Remember the phrase 'amigo mío' (friend of mine). Use 'amiga' for female friends."
                )
            )
        )

        // FRENCH VOCABULARY
        words.addAll(
            listOf(
                VocabWord(
                    language = "French",
                    word = "Une pomme",
                    phonetic = "oon pohm",
                    translation = "an apple",
                    category = "Food",
                    exampleSentence = "C'est une pomme rouge.",
                    exampleTranslation = "It is a red apple.",
                    audioPrompt = "Une pomme",
                    teacherTip = "Maya's Tip: Notice the 'e' at the end of 'pomme' is silent. Potatoes in French are charmingly called 'pommes de terre' (apples of the earth)!"
                ),
                VocabWord(
                    language = "French",
                    word = "Le livre",
                    phonetic = "luh leevr",
                    translation = "the book",
                    category = "Basics",
                    exampleSentence = "J'adore ce livre.",
                    exampleTranslation = "I love this book.",
                    audioPrompt = "Le livre",
                    teacherTip = "Maya's Tip: Softly pronounce the 'r' at the back of the throat. 'Livre' is masculine, taking 'le'."
                ),
                VocabWord(
                    language = "French",
                    word = "Bonjour",
                    phonetic = "bohn-zhoor",
                    translation = "hello",
                    category = "Greetings",
                    exampleSentence = "Bonjour mon ami!",
                    exampleTranslation = "Hello my friend!",
                    audioPrompt = "Bonjour",
                    teacherTip = "Maya's Tip: Literally means 'good day' ('bon' + 'jour'). In France, greeting with 'Bonjour' before asking a question is paramount polite etiquette."
                ),
                VocabWord(
                    language = "French",
                    word = "Eau",
                    phonetic = "oh",
                    translation = "water",
                    category = "Food",
                    exampleSentence = "De l'eau s'il vous plaît.",
                    exampleTranslation = "Water please.",
                    audioPrompt = "Eau",
                    teacherTip = "Maya's Tip: Three letters ('e-a-u') make just one pure single vowel sound: 'OH'! French vowel harmony at its finest."
                ),
                VocabWord(
                    language = "French",
                    word = "Merci",
                    phonetic = "mair-see",
                    translation = "thank you",
                    category = "Greetings",
                    exampleSentence = "Merci beaucoup!",
                    exampleTranslation = "Thank you very much!",
                    audioPrompt = "Merci",
                    teacherTip = "Maya's Tip: Pair it with 'beaucoup' (meaning 'a lot') to say 'Merci beaucoup' with a smile!"
                ),
                VocabWord(
                    language = "French",
                    word = "Un chat",
                    phonetic = "un shah",
                    translation = "a cat",
                    category = "Animals",
                    exampleSentence = "Le petit chat noir.",
                    exampleTranslation = "The little black cat.",
                    audioPrompt = "Un chat",
                    teacherTip = "Maya's Tip: The final 't' is completely silent! Say 'shah'. For a female cat, say 'une chatte' (where the 't' sound is spoken)."
                )
            )
        )

        // JAPANESE VOCABULARY
        words.addAll(
            listOf(
                VocabWord(
                    language = "Japanese",
                    word = "りんご",
                    phonetic = "ringo",
                    translation = "apple",
                    category = "Food",
                    exampleSentence = "おいしいりんごです。",
                    exampleTranslation = "It is a delicious apple.",
                    audioPrompt = "りんご",
                    teacherTip = "Maya's Tip: Japanese 'r' sound is a light flick between English 'l' and 'd'. Japanese apples (especially Fuji apples) are renowned for sweetness."
                ),
                VocabWord(
                    language = "Japanese",
                    word = "ほん",
                    phonetic = "hon",
                    translation = "book",
                    category = "Basics",
                    exampleSentence = "これは私の本です。",
                    exampleTranslation = "This is my book.",
                    audioPrompt = "ほん",
                    teacherTip = "Maya's Tip: 'Hon' (本) is short and punchy. Japanese readers traditionally read vertically from right to left in classic literature!"
                ),
                VocabWord(
                    language = "Japanese",
                    word = "こんにちは",
                    phonetic = "konnichiwa",
                    translation = "hello",
                    category = "Greetings",
                    exampleSentence = "みなさん、こんにちは！",
                    exampleTranslation = "Hello everyone!",
                    audioPrompt = "こんにちは",
                    teacherTip = "Maya's Tip: Notice the final character is written with 'ha' (は) but pronounced 'wa' because it serves as the topic marker particle."
                ),
                VocabWord(
                    language = "Japanese",
                    word = "みず",
                    phonetic = "mizu",
                    translation = "water",
                    category = "Food",
                    exampleSentence = "冷たいお水をください。",
                    exampleTranslation = "Please give me cold water.",
                    audioPrompt = "みず",
                    teacherTip = "Maya's Tip: In restaurants, politely prefix it with 'o-' ('omizu') to show respect for the nourishment."
                ),
                VocabWord(
                    language = "Japanese",
                    word = "ありがとう",
                    phonetic = "arigatou",
                    translation = "thank you",
                    category = "Greetings",
                    exampleSentence = "いつもありがとう！",
                    exampleTranslation = "Thank you always!",
                    audioPrompt = "ありがとう",
                    teacherTip = "Maya's Tip: Add 'gozaimasu' ('Arigatou gozaimasu') to make it formal and respectful to teachers and strangers."
                ),
                VocabWord(
                    language = "Japanese",
                    word = "ねこ",
                    phonetic = "neko",
                    translation = "cat",
                    category = "Animals",
                    exampleSentence = "可愛い猫がいます。",
                    exampleTranslation = "There is a cute cat.",
                    audioPrompt = "ねこ",
                    level = "A1",
                    teacherTip = "Maya's Tip: 'Neko' (猫) are beloved in Japan! You'll often see 'Maneki-neko' (the welcoming beckoning cat figurine) bringing good fortune."
                )
            )
        )

        // GERMAN VOCABULARY
        words.addAll(
            listOf(
                VocabWord(
                    language = "German",
                    word = "Guten Tag",
                    phonetic = "goo-ten tahk",
                    translation = "good day",
                    category = "Greetings",
                    exampleSentence = "Guten Tag! Wie geht es Ihnen?",
                    exampleTranslation = "Good day! How are you?",
                    audioPrompt = "Guten Tag",
                    level = "A1",
                    teacherTip = "Maya's Tip: German capitalizes every single noun! 'Tag' is always written with a capital T."
                ),
                VocabWord(
                    language = "German",
                    word = "Apfel",
                    phonetic = "up-fel",
                    translation = "apple",
                    category = "Food",
                    exampleSentence = "Ich esse einen Apfel.",
                    exampleTranslation = "I eat an apple.",
                    audioPrompt = "Apfel",
                    level = "A1",
                    teacherTip = "Maya's Tip: 'Der Apfel' is masculine. In the direct object accusative case, 'ein' becomes 'einen'."
                ),
                VocabWord(
                    language = "German",
                    word = "Danke",
                    phonetic = "dahn-keh",
                    translation = "thank you",
                    category = "Greetings",
                    exampleSentence = "Vielen Dank für Ihre Hilfe.",
                    exampleTranslation = "Many thanks for your help.",
                    audioPrompt = "Danke",
                    level = "A1",
                    teacherTip = "Maya's Tip: Use 'Danke schön' for 'Thank you kindly', and answer with 'Bitte schön' ('You're welcome')."
                ),
                VocabWord(
                    language = "German",
                    word = "Wasser",
                    phonetic = "vah-ser",
                    translation = "water",
                    category = "Food",
                    exampleSentence = "Ein Glas kaltes Wasser bitte.",
                    exampleTranslation = "A glass of cold water please.",
                    audioPrompt = "Wasser",
                    level = "A1",
                    teacherTip = "Maya's Tip: In Germany, if you order 'Wasser', you'll usually get sparkling water (mit Kohlensäure). For flat, ask for 'stilles Wasser'!"
                )
            )
        )

        // ITALIAN VOCABULARY
        words.addAll(
            listOf(
                VocabWord(
                    language = "Italian",
                    word = "ciao",
                    phonetic = "chow",
                    translation = "hello",
                    category = "Greetings",
                    exampleSentence = "Ciao, come stai?",
                    exampleTranslation = "Hi, how are you?",
                    audioPrompt = "ciao",
                    level = "A1",
                    teacherTip = "Maya's Tip: 'Ciao' works for both 'hello' and 'bye' with friends. Use 'Buongiorno' for formal respect."
                ),
                VocabWord(
                    language = "Italian",
                    word = "grazie",
                    phonetic = "grah-tsee-eh",
                    translation = "thank you",
                    category = "Greetings",
                    exampleSentence = "Mille grazie per la cena.",
                    exampleTranslation = "A thousand thanks for dinner.",
                    audioPrompt = "grazie",
                    level = "A1",
                    teacherTip = "Maya's Tip: Pronounce all three syllables: gra-tzi-e. Don't drop the final 'e'!"
                ),
                VocabWord(
                    language = "Italian",
                    word = "mela",
                    phonetic = "meh-lah",
                    translation = "apple",
                    category = "Food",
                    exampleSentence = "Mangio una mela dolce.",
                    exampleTranslation = "I eat a sweet apple.",
                    audioPrompt = "mela",
                    level = "A1",
                    teacherTip = "Maya's Tip: 'La mela' is feminine. The plural is 'le mele'."
                ),
                VocabWord(
                    language = "Italian",
                    word = "caffè",
                    phonetic = "kahf-feh",
                    translation = "coffee",
                    category = "Food",
                    exampleSentence = "Un caffè espresso, per favore.",
                    exampleTranslation = "An espresso coffee, please.",
                    audioPrompt = "caffè",
                    level = "A1",
                    teacherTip = "Maya's Tip: In Italy, simply asking for 'un caffè' gets you a rich, concentrated single shot of espresso!"
                )
            )
        )

        // KOREAN VOCABULARY
        words.addAll(
            listOf(
                VocabWord(
                    language = "Korean",
                    word = "안녕하세요",
                    phonetic = "annyeonghaseyo",
                    translation = "hello",
                    category = "Greetings",
                    exampleSentence = "안녕하세요! 반갑습니다.",
                    exampleTranslation = "Hello! Nice to meet you.",
                    audioPrompt = "안녕하세요",
                    level = "A1",
                    teacherTip = "Maya's Tip: Annyeong means peace/wellness. A respectful slight bow makes your Korean greeting authentic."
                ),
                VocabWord(
                    language = "Korean",
                    word = "감사합니다",
                    phonetic = "gamsahamnida",
                    translation = "thank you",
                    category = "Greetings",
                    exampleSentence = "도와주셔서 감사합니다.",
                    exampleTranslation = "Thank you for helping me.",
                    audioPrompt = "감사합니다",
                    level = "A1",
                    teacherTip = "Maya's Tip: The polite standard thank you in business and social etiquette. The 'ㅂ' sounds like 'm' before 'ㄴ'!"
                ),
                VocabWord(
                    language = "Korean",
                    word = "사과",
                    phonetic = "sagwa",
                    translation = "apple",
                    category = "Food",
                    exampleSentence = "맛있는 사과를 먹어요.",
                    exampleTranslation = "I eat a delicious apple.",
                    audioPrompt = "사과",
                    level = "A1",
                    teacherTip = "Maya's Tip: Fun fact! 'Sagwa' (사과) means both 'apple' and 'apology' in Korean. Giving an apple can be a playful peace offering!"
                ),
                VocabWord(
                    language = "Korean",
                    word = "물",
                    phonetic = "mul",
                    translation = "water",
                    category = "Food",
                    exampleSentence = "시원한 물 주세요.",
                    exampleTranslation = "Please give me cool water.",
                    audioPrompt = "물",
                    level = "A1",
                    teacherTip = "Maya's Tip: In Korean restaurants, water is almost always self-service: '물은 셀프입니다' (mul-eun sel-peu-im-ni-da)."
                )
            )
        )

        // MANDARIN VOCABULARY
        words.addAll(
            listOf(
                VocabWord(
                    language = "Mandarin",
                    word = "你好",
                    phonetic = "nǐ hǎo",
                    translation = "hello",
                    category = "Greetings",
                    exampleSentence = "你好！今天天气真好。",
                    exampleTranslation = "Hello! The weather is really nice today.",
                    audioPrompt = "你好",
                    level = "A1",
                    teacherTip = "Maya's Tip: Tone Sandhi rule! Two 3rd tones in a row make the first tone rise like a 2nd tone ('ní hǎo')."
                ),
                VocabWord(
                    language = "Mandarin",
                    word = "谢谢",
                    phonetic = "xiè xie",
                    translation = "thank you",
                    category = "Greetings",
                    exampleSentence = "谢谢你的帮助！",
                    exampleTranslation = "Thank you for your help!",
                    audioPrompt = "谢谢",
                    level = "A1",
                    teacherTip = "Maya's Tip: The first 'xiè' is a sharp falling 4th tone, and the second is a light neutral tone. Reply with '不客气' (bù kèqi)."
                ),
                VocabWord(
                    language = "Mandarin",
                    word = "苹果",
                    phonetic = "píng guǒ",
                    translation = "apple",
                    category = "Food",
                    exampleSentence = "我吃了一个红苹果。",
                    exampleTranslation = "I ate a red apple.",
                    audioPrompt = "苹果",
                    level = "A1",
                    teacherTip = "Maya's Tip: Apples symbolize peace ('píng' sounds like 'píng'ān' - safety/peace). People exchange decorated apples on Christmas Eve in China!"
                ),
                VocabWord(
                    language = "Mandarin",
                    word = "茶",
                    phonetic = "chá",
                    translation = "tea",
                    category = "Food",
                    exampleSentence = "请喝一杯热茶。",
                    exampleTranslation = "Please drink a cup of hot tea.",
                    audioPrompt = "茶",
                    level = "A1",
                    teacherTip = "Maya's Tip: 2nd rising tone. When someone pours you tea at dim sum, tap your index and middle fingers on the table as a silent thank you!"
                )
            )
        )

        words.addAll(extraLanguageStarterDecks())
        return words
    }

    /** Every language shown in the picker has a usable A1 starter deck. */
    private fun extraLanguageStarterDecks(): List<VocabWord> {
        val rows = listOf(
            arrayOf("Portuguese", "Olá", "oh-LAH", "hello"), arrayOf("Portuguese", "Obrigado", "oh-bree-GAH-doo", "thank you"), arrayOf("Portuguese", "Água", "AH-gwah", "water"), arrayOf("Portuguese", "Livro", "LEE-vroo", "book"), arrayOf("Portuguese", "Sim", "seem", "yes"),
            arrayOf("Arabic", "مرحبا", "mar-ha-ban", "hello"), arrayOf("Arabic", "شكرا", "shuk-ran", "thank you"), arrayOf("Arabic", "ماء", "maa", "water"), arrayOf("Arabic", "كتاب", "ki-taab", "book"), arrayOf("Arabic", "نعم", "na-am", "yes"),
            arrayOf("Russian", "Привет", "pree-VYET", "hello"), arrayOf("Russian", "Спасибо", "spa-SEE-ba", "thank you"), arrayOf("Russian", "Вода", "va-DAH", "water"), arrayOf("Russian", "Книга", "KNEE-ga", "book"), arrayOf("Russian", "Да", "dah", "yes"),
            arrayOf("Turkish", "Merhaba", "MEHR-ha-ba", "hello"), arrayOf("Turkish", "Teşekkürler", "te-shek-KUR-ler", "thank you"), arrayOf("Turkish", "Su", "soo", "water"), arrayOf("Turkish", "Kitap", "kee-TAP", "book"), arrayOf("Turkish", "Evet", "EH-vet", "yes"),
            arrayOf("English", "Hello", "heh-LOH", "hello"), arrayOf("English", "Thanks", "thanks", "thank you"), arrayOf("English", "Water", "WAH-ter", "water"), arrayOf("English", "Book", "book", "book"), arrayOf("English", "Yes", "yes", "yes"),
            arrayOf("Bengali", "নমস্কার", "nomosh-kar", "hello"), arrayOf("Bengali", "ধন্যবাদ", "dhon-no-bad", "thank you"), arrayOf("Bengali", "জল", "jol", "water"), arrayOf("Bengali", "বই", "boi", "book"), arrayOf("Bengali", "হ্যাঁ", "hyaa", "yes"),
            arrayOf("Tamil", "வணக்கம்", "va-nak-kam", "hello"), arrayOf("Tamil", "நன்றி", "nan-ri", "thank you"), arrayOf("Tamil", "தண்ணீர்", "than-neer", "water"), arrayOf("Tamil", "புத்தகம்", "puth-tha-gam", "book"), arrayOf("Tamil", "ஆம்", "aam", "yes"),
            arrayOf("Telugu", "నమస్కారం", "na-mas-ka-ram", "hello"), arrayOf("Telugu", "ధన్యవాదాలు", "dhan-ya-va-daalu", "thank you"), arrayOf("Telugu", "నీరు", "nee-ru", "water"), arrayOf("Telugu", "పుస్తకం", "pus-ta-kam", "book"), arrayOf("Telugu", "అవును", "a-vu-nu", "yes"),
            arrayOf("Marathi", "नमस्कार", "na-mas-kaar", "hello"), arrayOf("Marathi", "धन्यवाद", "dhan-ya-vaad", "thank you"), arrayOf("Marathi", "पाणी", "paa-nee", "water"), arrayOf("Marathi", "पुस्तक", "pus-tak", "book"), arrayOf("Marathi", "हो", "ho", "yes"),
            arrayOf("Gujarati", "નમસ્તે", "na-mas-te", "hello"), arrayOf("Gujarati", "આભાર", "aa-bhaar", "thank you"), arrayOf("Gujarati", "પાણી", "paa-nee", "water"), arrayOf("Gujarati", "પુસ્તક", "pus-tak", "book"), arrayOf("Gujarati", "હા", "haa", "yes"),
            arrayOf("Punjabi", "ਸਤ ਸ੍ਰੀ ਅਕਾਲ", "sat-sri-akal", "hello"), arrayOf("Punjabi", "ਧੰਨਵਾਦ", "dhan-na-vaad", "thank you"), arrayOf("Punjabi", "ਪਾਣੀ", "paa-nee", "water"), arrayOf("Punjabi", "ਕਿਤਾਬ", "ki-taab", "book"), arrayOf("Punjabi", "ਹਾਂ", "haan", "yes"),
            arrayOf("Vietnamese", "Xin chào", "sin-chow", "hello"), arrayOf("Vietnamese", "Cảm ơn", "kam-un", "thank you"), arrayOf("Vietnamese", "Nước", "nu-oc", "water"), arrayOf("Vietnamese", "Sách", "sach", "book"), arrayOf("Vietnamese", "Vâng", "vung", "yes"),
            arrayOf("Indonesian", "Halo", "ha-lo", "hello"), arrayOf("Indonesian", "Terima kasih", "te-ri-ma ka-sih", "thank you"), arrayOf("Indonesian", "Air", "ah-eer", "water"), arrayOf("Indonesian", "Buku", "boo-koo", "book"), arrayOf("Indonesian", "Ya", "yah", "yes"),
            arrayOf("Dutch", "Hallo", "ha-lo", "hello"), arrayOf("Dutch", "Dank je", "dank yuh", "thank you"), arrayOf("Dutch", "Water", "vah-ter", "water"), arrayOf("Dutch", "Boek", "book", "book"), arrayOf("Dutch", "Ja", "yah", "yes"),
            arrayOf("Thai", "สวัสดี", "sa-wat-dee", "hello"), arrayOf("Thai", "ขอบคุณ", "khop-khun", "thank you"), arrayOf("Thai", "น้ำ", "naam", "water"), arrayOf("Thai", "หนังสือ", "nang-sue", "book"), arrayOf("Thai", "ใช่", "chai", "yes")
        )
        return rows.map { (language, word, phonetic, translation) ->
            VocabWord(
                language = language, word = word, phonetic = phonetic, translation = translation,
                category = "Essentials", exampleSentence = word, exampleTranslation = translation,
                audioPrompt = word, level = "A1", teacherTip = "Nyra's Tip: Use this word in a short sentence today."
            )
        }
    }
}
