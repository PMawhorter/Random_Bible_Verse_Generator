package com.example.randombibleverse

/**
 * Enumeration of supported Bible versification schemes.
 * Each scheme represents a different way books and chapters are organized.
 */
enum class VersificationScheme(val label: String) {
    KJV("KJV (NKJV, ESV, NIV, NASB...)"),
    NRSV("NRSV (RSV, NRSVue)"),
    CATHOLIC("Catholic (NABRE)"),
    ORTHODOX("Orthodox (Russian / Synodal)"),
    HEBREW("Hebrew (Tanakh / MT)")
}

/**
 * Represents a chapter identified by a label (e.g., "A", "B" in Catholic Esther)
 * rather than a standard integer.
 */
data class LabeledChapter(val label: String, val maxVerse: Int)

/**
 * Central repository for Bible structure data, including abbreviations,
 * chapter/verse counts for various canons, and logic for different versification schemes.
 */
object BibleData {

    /**
     * Map of full book names to their standard OSIS abbreviations.
     */
    val osisAbbreviations = mapOf(
        "Genesis" to "Gen", "Exodus" to "Exod", "Leviticus" to "Lev", "Numbers" to "Num", "Deuteronomy" to "Deut",
        "Joshua" to "Josh", "Judges" to "Judg", "Ruth" to "Ruth", "1 Samuel" to "1Sam", "2 Samuel" to "2Sam",
        "1 Kings" to "1Kgs", "2 Kings" to "2Kgs", "1 Chronicles" to "1Chr", "2 Chronicles" to "2Chr",
        "Ezra" to "Ezra", "Nehemiah" to "Neh", "Esther" to "Esth", "Job" to "Job", "Psalms" to "Ps",
        "Proverbs" to "Prov", "Ecclesiastes" to "Eccl", "Song of Solomon" to "Song", "Isaiah" to "Isa",
        "Jeremiah" to "Jer", "Lamentations" to "Lam", "Ezekiel" to "Ezek", "Daniel" to "Dan",
        "Hosea" to "Hos", "Joel" to "Joel", "Amos" to "Amos", "Obadiah" to "Obad", "Jonah" to "Jonah",
        "Micah" to "Mic", "Nahum" to "Nah", "Habakkuk" to "Hab", "Zephaniah" to "Zeph", "Haggai" to "Hag",
        "Zechariah" to "Zech", "Malachi" to "Mal", "Matthew" to "Matt", "Mark" to "Mark", "Luke" to "Luke",
        "John" to "John", "Acts" to "Acts", "Romans" to "Rom", "1 Corinthians" to "1Cor", "2 Corinthians" to "2Cor",
        "Galatians" to "Gal", "Ephesians" to "Eph", "Philippians" to "Phil", "Colossians" to "Col",
        "1 Thessalonians" to "1Thess", "2 Thessalonians" to "2Thess", "1 Timothy" to "1Tim", "2 Timothy" to "2Tim",
        "Titus" to "Titus", "Philemon" to "Phlm", "Hebrews" to "Heb", "James" to "Jas", "1 Peter" to "1Pet",
        "2 Peter" to "2Pet", "1 John" to "1John", "2 John" to "2John", "3 John" to "3John", "Jude" to "Jude",
        "Revelation" to "Rev", "Tobit" to "Tob", "Judith" to "Jdt", "Wisdom of Solomon" to "Wis",
        "Sirach" to "Sir", "Baruch" to "Bar", "1 Maccabees" to "1Macc", "2 Maccabees" to "2Macc",
        "3 Maccabees" to "3Macc", "1 Esdras" to "1Esd", "2 Esdras" to "2Esd", "Psalm 151" to "Ps151",
    )

    /**
     * Map of full book names to their standard USFM/three-letter abbreviations.
     */
    val bookAbbreviations = mapOf(
        "Genesis" to "GEN", "Exodus" to "EXO", "Leviticus" to "LEV", "Numbers" to "NUM", "Deuteronomy" to "DEU",
        "Joshua" to "JOS", "Judges" to "JDG", "Ruth" to "RUT", "1 Samuel" to "1SA", "2 Samuel" to "2SA",
        "1 Kings" to "1KI", "2 Kings" to "2KI", "1 Chronicles" to "1CH", "2 Chronicles" to "2CH",
        "Ezra" to "EZR", "Nehemiah" to "NEH", "Esther" to "EST", "Job" to "JOB", "Psalms" to "PSA",
        "Proverbs" to "PRO", "Ecclesiastes" to "ECC", "Song of Solomon" to "SNG", "Isaiah" to "ISA",
        "Jeremiah" to "JER", "Lamentations" to "LAM", "Ezekiel" to "EZK", "Daniel" to "DAN",
        "Hosea" to "HOS", "Joel" to "JOL", "Amos" to "AMO", "Obadiah" to "OBA", "Jonah" to "JON",
        "Micah" to "MIC", "Nahum" to "NAM", "Habakkuk" to "HAB", "Zephaniah" to "ZEP", "Haggai" to "HAG",
        "Zechariah" to "ZEC", "Malachi" to "MAL", "Matthew" to "MAT", "Mark" to "MRK", "Luke" to "LUK",
        "John" to "JHN", "Acts" to "ACT", "Romans" to "ROM", "1 Corinthians" to "1CO", "2 Corinthians" to "2CO",
        "Galatians" to "GAL", "Ephesians" to "EPH", "Philippians" to "PHP", "Colossians" to "COL",
        "1 Thessalonians" to "1TH", "2 Thessalonians" to "2TH", "1 Timothy" to "1TI", "2 Timothy" to "2TI",
        "Titus" to "TIT", "Philemon" to "PHM", "Hebrews" to "HEB", "James" to "JAS", "1 Peter" to "1PE",
        "2 Peter" to "2PE", "1 John" to "1JN", "2 John" to "2JN", "3 John" to "3JN", "Jude" to "JUD",
        "Revelation" to "REV", "Tobit" to "TOB", "Judith" to "JDT", "Wisdom of Solomon" to "WIS",
        "Sirach" to "SIR", "Baruch" to "BAR", "1 Maccabees" to "1MA", "2 Maccabees" to "2MA",
        "3 Maccabees" to "3MA", "1 Esdras" to "1ES", "2 Esdras" to "2ES", "Psalm 151" to "P15",
    )

    /**
     * Chapter and verse counts for the standard Protestant/KJV Old Testament canon.
     */
    val oldTestament = mapOf(
        "Genesis" to listOf(31, 25, 24, 26, 32, 22, 24, 22, 29, 32, 32, 20, 18, 24, 21, 16, 27, 33, 38, 34, 24, 20, 67, 34, 35, 46, 22, 35, 43, 55, 32, 20, 31, 29, 43, 36, 30, 26, 23, 57, 38, 34, 31, 28, 34, 31, 22, 33, 26),
        "Exodus" to listOf(22, 25, 22, 31, 23, 30, 29, 28, 35, 29, 10, 51, 22, 31, 27, 36, 16, 27, 25, 26, 36, 31, 33, 18, 40, 37, 21, 43, 45, 38, 18, 35, 23, 35, 35, 38, 29, 31, 43, 38),
        "Leviticus" to listOf(17, 16, 17, 35, 19, 30, 38, 36, 24, 20, 47, 8, 59, 57, 33, 34, 16, 30, 37, 27, 24, 33, 44, 23, 55, 46, 34),
        "Numbers" to listOf(54, 34, 51, 49, 31, 27, 89, 26, 23, 36, 35, 16, 33, 45, 41, 50, 13, 32, 22, 29, 35, 41, 30, 25, 18, 65, 23, 31, 40, 16, 54, 42, 56, 29, 34, 13),
        "Deuteronomy" to listOf(46, 37, 29, 43, 33, 25, 26, 20, 29, 22, 32, 32, 18, 29, 23, 22, 20, 22, 21, 20, 23, 30, 25, 22, 19, 19, 26, 68, 29, 20, 30, 52, 29, 12),
        "Joshua" to listOf(18, 24, 17, 24, 15, 27, 26, 35, 27, 43, 23, 24, 33, 15, 63, 10, 18, 28, 51, 9, 45, 34, 16, 33),
        "Judges" to listOf(36, 23, 31, 24, 31, 40, 25, 35, 57, 18, 40, 15, 25, 20, 20, 31, 13, 31, 30, 48, 25),
        "Ruth" to listOf(22, 23, 18, 22),
        "1 Samuel" to listOf(28, 36, 21, 22, 12, 21, 17, 22, 27, 27, 15, 25, 23, 52, 35, 23, 58, 30, 24, 42, 15, 23, 29, 22, 44, 25, 12, 25, 11, 31, 13),
        "2 Samuel" to listOf(27, 32, 39, 12, 25, 23, 29, 18, 13, 19, 27, 31, 39, 33, 37, 23, 29, 33, 43, 26, 22, 51, 39, 25),
        "1 Kings" to listOf(53, 46, 15, 34, 18, 38, 51, 66, 28, 29, 43, 33, 34, 31, 34, 34, 24, 46, 21, 43, 29, 53),
        "2 Kings" to listOf(18, 25, 27, 44, 27, 33, 20, 29, 37, 36, 21, 21, 25, 29, 38, 20, 41, 37, 37, 21, 26, 20, 37, 20, 30),
        "1 Chronicles" to listOf(43, 55, 24, 43, 26, 81, 40, 40, 44, 14, 47, 40, 14, 17, 29, 43, 27, 17, 19, 8, 30, 31, 32, 31, 31, 32, 34, 21, 30),
        "2 Chronicles" to listOf(17, 18, 17, 22, 14, 42, 22, 18, 31, 19, 23, 16, 23, 15, 19, 14, 19, 34, 11, 37, 20, 12, 21, 27, 34, 23, 9, 27, 36, 27, 21, 33, 25, 33, 27, 23),
        "Ezra" to listOf(11, 15, 13, 24, 17, 22, 28, 36, 15, 44),
        "Nehemiah" to listOf(11, 20, 32, 23, 19, 19, 73, 18, 38, 39, 36, 47, 31),
        "Esther" to listOf(22, 23, 15, 17, 14, 14, 10, 17, 32, 3),
        "Job" to listOf(22, 13, 26, 21, 27, 30, 21, 22, 35, 22, 20, 25, 28, 22, 35, 22, 16, 21, 29, 29, 34, 30, 17, 25, 6, 14, 23, 28, 25, 31, 40, 22, 33, 37, 16, 33, 24, 41, 30, 32, 34, 17),
        "Psalms" to listOf(6, 12, 8, 8, 12, 10, 17, 9, 20, 18, 7, 8, 6, 7, 5, 11, 15, 50, 14, 9, 13, 31, 6, 10, 22, 12, 14, 9, 11, 12, 24, 11, 22, 22, 28, 12, 40, 22, 13, 17, 13, 11, 5, 26, 17, 11, 9, 14, 20, 23, 19, 9, 6, 7, 23, 13, 11, 11, 17, 12, 8, 12, 11, 10, 13, 20, 7, 35, 5, 24, 13, 28, 20, 12, 19, 10, 72, 13, 19, 10, 9, 18, 18, 12, 13, 17, 7, 18, 52, 17, 16, 15, 5, 23, 11, 13, 12, 9, 9, 8, 8, 28, 22, 35, 45, 48, 43, 13, 31, 7, 10, 10, 9, 8, 18, 19, 2, 29, 176, 7, 8, 9, 4, 8, 5, 6, 5, 8, 8, 3, 18, 3, 3, 21, 13, 10, 9, 8, 24, 13, 10, 7, 12, 15, 21, 10, 20, 14, 9, 6),
        "Proverbs" to listOf(33, 22, 35, 27, 23, 35, 27, 36, 18, 32, 31, 28, 25, 35, 33, 33, 28, 24, 29, 30, 31, 29, 35, 34, 28, 28, 27, 26, 27, 33, 31),
        "Ecclesiastes" to listOf(18, 26, 22, 16, 20, 12, 29, 17, 18, 20, 10, 14),
        "Song of Solomon" to listOf(17, 17, 11, 16, 16, 13, 13, 14),
        "Isaiah" to listOf(31, 22, 26, 6, 30, 13, 25, 22, 21, 34, 16, 6, 22, 32, 9, 14, 14, 7, 25, 6, 17, 25, 18, 23, 12, 21, 13, 29, 24, 33, 9, 20, 24, 17, 10, 22, 38, 13, 8, 31, 29, 25, 28, 28, 25, 13, 15, 22, 26, 11, 23, 15, 12, 17, 13, 12, 21, 14, 21, 22, 11, 12, 19, 12, 25, 24),
        "Jeremiah" to listOf(19, 37, 25, 31, 31, 30, 34, 22, 26, 25, 23, 17, 27, 26, 21, 21, 27, 23, 15, 18, 14, 30, 40, 10, 38, 24, 22, 17, 35, 24, 40, 44, 26, 22, 19, 32, 21, 28, 18, 16, 18, 22, 13, 30, 5, 28, 7, 45, 39, 46, 64, 34),
        "Lamentations" to listOf(22, 22, 66, 22, 22),
        "Ezekiel" to listOf(28, 10, 27, 17, 17, 14, 27, 18, 11, 22, 25, 28, 23, 23, 8, 63, 24, 32, 14, 49, 32, 31, 49, 27, 17, 21, 36, 26, 21, 26, 18, 32, 33, 31, 15, 38, 28, 23, 29, 49, 26, 20, 27, 31, 25, 24, 23, 35),
        "Daniel" to listOf(21, 49, 30, 37, 31, 28, 28, 27, 27, 21, 45, 13),
        "Hosea" to listOf(11, 23, 5, 19, 15, 11, 16, 14, 17, 15, 11, 14, 16, 9),
        "Joel" to listOf(20, 32, 21),
        "Amos" to listOf(15, 16, 15, 13, 27, 14, 17, 14, 15),
        "Obadiah" to listOf(21),
        "Jonah" to listOf(17, 10, 10, 11),
        "Micah" to listOf(16, 13, 12, 13, 15, 16, 20),
        "Nahum" to listOf(14, 13, 19),
        "Habakkuk" to listOf(17, 20, 19),
        "Zephaniah" to listOf(18, 15, 20),
        "Haggai" to listOf(15, 23),
        "Zechariah" to listOf(21, 13, 10, 14, 11, 15, 14, 23, 17, 12, 17, 14, 9, 21),
        "Malachi" to listOf(14, 17, 18, 6),
    )

    /**
     * Chapter and verse counts for the standard Protestant/KJV New Testament canon.
     */
    val newTestament = mapOf(
        "Matthew" to listOf(25, 23, 17, 25, 48, 34, 29, 34, 38, 42, 30, 50, 58, 36, 39, 28, 27, 35, 30, 34, 46, 46, 39, 51, 46, 75, 66, 20),
        "Mark" to listOf(45, 28, 35, 41, 43, 56, 37, 38, 50, 52, 33, 44, 37, 72, 47, 20),
        "Luke" to listOf(80, 52, 38, 44, 39, 49, 50, 56, 62, 42, 54, 59, 35, 35, 32, 31, 37, 43, 48, 47, 38, 71, 56, 53),
        "John" to listOf(51, 25, 36, 54, 47, 71, 53, 59, 41, 42, 57, 50, 38, 31, 27, 33, 26, 40, 42, 31, 25),
        "Acts" to listOf(26, 47, 26, 37, 42, 15, 60, 40, 43, 48, 30, 25, 52, 28, 41, 40, 34, 28, 41, 38, 40, 30, 35, 27, 27, 32, 44, 31),
        "Romans" to listOf(32, 29, 31, 25, 21, 23, 25, 39, 33, 21, 36, 21, 14, 23, 33, 27),
        "1 Corinthians" to listOf(31, 16, 23, 21, 13, 20, 40, 13, 27, 33, 34, 31, 13, 40, 58, 24),
        "2 Corinthians" to listOf(24, 17, 18, 18, 21, 18, 16, 24, 15, 18, 33, 21, 14),
        "Galatians" to listOf(24, 21, 29, 31, 26, 18),
        "Ephesians" to listOf(23, 22, 21, 32, 33, 24),
        "Philippians" to listOf(30, 30, 21, 23),
        "Colossians" to listOf(29, 23, 25, 18),
        "1 Thessalonians" to listOf(10, 20, 13, 18, 28),
        "2 Thessalonians" to listOf(12, 17, 18),
        "1 Timothy" to listOf(20, 15, 16, 16, 25, 21),
        "2 Timothy" to listOf(18, 26, 17, 22),
        "Titus" to listOf(16, 15, 15),
        "Philemon" to listOf(25),
        "Hebrews" to listOf(14, 18, 19, 16, 14, 20, 28, 13, 28, 39, 40, 29, 25),
        "James" to listOf(27, 26, 18, 17, 20),
        "1 Peter" to listOf(25, 25, 22, 19, 14),
        "2 Peter" to listOf(21, 22, 18),
        "1 John" to listOf(10, 29, 24, 21, 21),
        "2 John" to listOf(13),
        "3 John" to listOf(14),
        "Jude" to listOf(25),
        "Revelation" to listOf(20, 29, 22, 11, 14, 17, 17, 13, 21, 11, 19, 17, 18, 20, 8, 21, 18, 24, 21, 15, 27, 21),
    )

    /**
     * Council of Trent deuterocanon - the books actually in the Catholic Bible.
     */
    val catholicApocrypha = mapOf(
        "Tobit" to listOf(22, 23, 17, 21, 22, 18, 17, 21, 12, 13, 19, 22, 18, 15),
        "Judith" to listOf(16, 28, 10, 16, 24, 21, 32, 36, 14, 23, 19, 20, 31, 19),
        "Wisdom of Solomon" to listOf(16, 24, 19, 20, 23, 25, 30, 18, 19, 21, 20, 27, 19, 31, 19, 29, 21, 25, 22),
        "Sirach" to listOf(30, 18, 31, 31, 15, 17, 36, 19, 17, 19, 28, 18, 26, 21, 20, 23, 32, 33, 18, 31, 28, 33, 27, 34, 29, 28, 26, 27, 28, 33, 40, 24, 42, 20, 26, 31, 31, 23, 35, 30, 30, 25, 27, 29, 25, 28, 28, 25, 35, 29, 30),
        "Baruch" to listOf(15, 35, 38, 37, 9, 73),
        "1 Maccabees" to listOf(64, 70, 60, 61, 68, 63, 50, 32, 73, 89, 71, 43, 53, 49, 41, 24),
        "2 Maccabees" to listOf(36, 32, 40, 61, 27, 31, 42, 36, 29, 38, 38, 45, 27, 46, 39),
    )

    /**
     * Books added by the Russian Synodal / Russian Orthodox Old Testament.
     */
    val russianOrthodoxExtras = mapOf(
        "1 Esdras" to listOf(58, 30, 24, 63, 73, 34, 15, 96, 55),
        "2 Esdras" to listOf(40, 48, 36, 52, 56, 59, 140, 63, 47, 59, 46, 51, 58, 48, 63, 78),
        "3 Maccabees" to listOf(29, 33, 29, 21, 51, 25, 23),
        "Psalm 151" to listOf(7),
    )

    /**
     * Greek additions to Daniel (shared by Catholic and Orthodox).
     */
    private val danielWithGreekAdditions: List<Int> =
        oldTestament.getValue("Daniel") + listOf(64, 42)

    /**
     * Catholic (NABRE) Esther with lettered Greek Additions.
     */
    val catholicEsther: List<LabeledChapter> = listOf(
        LabeledChapter("A", 17),
        LabeledChapter("1", 22),
        LabeledChapter("2", 23),
        LabeledChapter("3", 15),
        LabeledChapter("B", 7),
        LabeledChapter("4", 17),
        LabeledChapter("C", 30),
        LabeledChapter("D", 16),
        LabeledChapter("5", 14),
        LabeledChapter("6", 14),
        LabeledChapter("7", 10),
        LabeledChapter("8", 17),
        LabeledChapter("E", 24),
        LabeledChapter("9", 32),
        LabeledChapter("10", 3),
        LabeledChapter("F", 11),
    )

    /**
     * Accurate Masoretic Text Psalms verse count mapping.
     */
    private val masoreticPsalms = listOf(
        6, 12, 9, 9, 13, 11, 18, 10, 21, 18, 7, 9, 6, 7, 5, 11, 15, 51, 15, 10, 14, 32, 6, 10, 22, 12, 14, 10, 11, 13, 25, 11, 22, 23, 28, 13, 41, 23, 14, 18, 14, 12, 5, 27, 18, 12, 10, 15, 21, 24, 21, 10, 7, 8, 24, 14, 12, 12, 18, 13, 9, 13, 12, 11, 14, 21, 8, 36, 6, 24, 14, 29, 21, 13, 20, 11, 73, 13, 20, 10, 10, 19, 19, 13, 14, 18, 8, 19, 53, 18, 17, 16, 5, 23, 12, 14, 13, 9, 9, 8, 8, 28, 22, 35, 45, 48, 43, 13, 31, 7, 10, 10, 9, 8, 18, 19, 2, 29, 176, 7, 8, 9, 4, 8, 5, 6, 5, 8, 8, 3, 18, 3, 3, 21, 13, 10, 9, 8, 24, 13, 10, 7, 12, 15, 21, 10, 20, 14, 9, 6,
    )

    /**
     * Septuagint / Slavonic-Vulgate Psalm renumbering.
     */
    private val lxxPsalms = listOf(
        6, 12, 8, 8, 12, 10, 17, 9, 38, 7, 8, 6, 7, 5, 11, 15, 50, 14, 9, 13, 31, 6, 10, 22, 12, 14, 9, 11, 12, 24, 11, 22, 22, 28, 12, 40, 22, 13, 17, 13, 11, 5, 26, 17, 11, 9, 14, 20, 23, 19, 9, 6, 7, 23, 13, 11, 11, 17, 12, 8, 12, 11, 10, 13, 20, 7, 35, 5, 24, 13, 28, 20, 12, 19, 10, 72, 13, 19, 10, 9, 18, 18, 12, 13, 17, 7, 18, 52, 17, 16, 15, 5, 23, 11, 13, 12, 9, 9, 8, 8, 28, 22, 35, 45, 48, 43, 13, 31, 7, 10, 10, 9, 8, 18, 28, 29, 176, 7, 8, 9, 4, 8, 5, 6, 5, 8, 8, 3, 18, 3, 3, 11, 10, 13, 10, 9, 8, 24, 13, 10, 7, 11, 11, 12, 15, 21, 10, 20, 14, 9, 6,
    )

    /**
     * Cached computed maps for each versification scheme to optimize performance.
     */
    private val libraries: Map<VersificationScheme, Map<String, List<Int>>> by lazy {
        val baseKjv = oldTestament + newTestament
        val hebrew = oldTestament + mapOf(
            "Joel" to listOf(20, 27, 5, 21),
            "Malachi" to listOf(14, 17, 24),
            "Psalms" to masoreticPsalms,
        )
        val catholic = oldTestament + newTestament + catholicApocrypha + mapOf(
            "Psalms" to masoreticPsalms,
            "Daniel" to danielWithGreekAdditions,
        )
        val orthodox = oldTestament + newTestament + catholicApocrypha + russianOrthodoxExtras + mapOf(
            "Psalms" to lxxPsalms,
            "Daniel" to danielWithGreekAdditions,
        )

        mapOf(
            VersificationScheme.KJV to baseKjv,
            VersificationScheme.NRSV to baseKjv, // NRSV specific logic can be added here if needed
            VersificationScheme.HEBREW to hebrew,
            VersificationScheme.CATHOLIC to catholic,
            VersificationScheme.ORTHODOX to orthodox,
        )
    }

    /**
     * Retrieves the entire book structure map for a given scheme.
     * Note: Special cases like Catholic Esther require separate handling.
     */
    fun getLibrary(scheme: VersificationScheme): Map<String, List<Int>> {
        return libraries[scheme] ?: emptyMap()
    }

    /**
     * Returns the ordered list of labeled chapters for a specific book/scheme combination.
     * Currently primarily used for lettered additions in Catholic Esther.
     */
    fun getLabeledChapters(scheme: VersificationScheme, book: String): List<LabeledChapter>? {
        if ((scheme == VersificationScheme.CATHOLIC) && (book == "Esther")) {
            return catholicEsther
        }
        return null
    }
}
