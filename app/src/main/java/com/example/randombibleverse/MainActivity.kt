package com.example.randombibleverse

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.core.net.toUri
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * The main theme wrapper for the application, applying a Material 3 color scheme
 * based on the system's dark mode setting.
 */
@Composable
fun BibleAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = Color(0xFFD0BCFF),
            onPrimary = Color(0xFF381E72),
            surface = Color(0xFF1C1B1F),
            onSurface = Color(0xFFE6E1E5),
            outline = Color(0xFF938F99)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF6750A4),
            onPrimary = Color(0xFFFFFFFF),
            surface = Color(0xFFF7F7F7),
            onSurface = Color(0xFF1C1B1F),
            outline = Color(0xFFCCCCCC)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

/**
 * Enumeration of available book filters (All, OT, NT, Apocrypha).
 * Each type is associated with a string resource for its label.
 */
enum class FilterType(val labelResId: Int) {
    ALL(R.string.all_books),
    OLD_TESTAMENT(R.string.old_testament),
    NEW_TESTAMENT(R.string.new_testament),
    APOCRYPHA(R.string.apocrypha)
}

/**
 * Main Activity for the Random Bible Verse application.
 * Initializes the UI using Jetpack Compose.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BibleAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    BibleReferenceScreen()
                }
            }
        }
    }
}

/**
 * The primary screen for generating and displaying Bible references.
 * Handles state for selected schemes, filters, and current verse reference.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibleReferenceScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }

    // State for the selected versification scheme, persisted in SharedPreferences
    var selectedScheme by remember {
        mutableStateOf(
            try {
                VersificationScheme.valueOf(
                    prefs.getString("versification_scheme", VersificationScheme.KJV.name)
                        ?: VersificationScheme.KJV.name
                )
            } catch (_: Exception) {
                VersificationScheme.KJV
            }
        )
    }

    // Available filters based on the selected versification scheme
    val filterOptions = remember(selectedScheme) {
        when (selectedScheme) {
            VersificationScheme.KJV, VersificationScheme.NRSV ->
                listOf(FilterType.ALL, FilterType.OLD_TESTAMENT, FilterType.NEW_TESTAMENT)
            VersificationScheme.CATHOLIC ->
                listOf(FilterType.ALL, FilterType.OLD_TESTAMENT, FilterType.NEW_TESTAMENT, FilterType.APOCRYPHA)
            VersificationScheme.ORTHODOX ->
                listOf(FilterType.ALL, FilterType.OLD_TESTAMENT, FilterType.NEW_TESTAMENT, FilterType.APOCRYPHA)
            VersificationScheme.HEBREW ->
                listOf(FilterType.OLD_TESTAMENT)
        }
    }

    var expanded by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(filterOptions[0]) }

    // Reset filter if it's no longer available for the current scheme (e.g. switching from Catholic to Hebrew)
    LaunchedEffect(selectedScheme) {
        if (selectedFilter !in filterOptions) {
            selectedFilter = filterOptions[0]
        }
    }

    var currentReference by remember { mutableStateOf<BibleReference?>(null) }
    var isSpinning by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAppChooser by remember { mutableStateOf(false) }

    val resultText = currentReference?.toString() ?: stringResource(R.string.welcome_text)

    // Displays the app chooser for verified passages
    if (showAppChooser && currentReference != null && currentReference!!.chapter.toIntOrNull() != null) {
        AppChooserDialog(
            context = context,
            currentReference = currentReference!!,
            onDismiss = { showAppChooser = false }
        )
    }

    // Displays the settings dialog for choosing versification schemes
    if (showSettingsDialog) {
        SettingsDialog(
            currentScheme = selectedScheme,
            onSchemeSelected = { scheme ->
                selectedScheme = scheme
                prefs.edit { putString("versification_scheme", scheme.name) }
            },
            onDismiss = { showSettingsDialog = false }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Settings button in top-right corner
        IconButton(
            onClick = { showSettingsDialog = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Dropdown for selecting book filter (All, OT, etc.)
            FilterDropdown(
                selectedFilter = selectedFilter,
                filterOptions = filterOptions,
                expanded = expanded,
                onExpandedChange = { expanded = it },
                onFilterSelected = { selectedFilter = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Box displaying the generated reference with a vertical roll animation
            ReferenceDisplay(resultText = resultText)

            Spacer(modifier = Modifier.height(24.dp))

            // Main interaction buttons
            ReferenceActionButtons(
                context = context,
                currentReference = currentReference,
                isSpinning = isSpinning,
                onGenerate = {
                    if (!isSpinning) {
                        scope.launch {
                            isSpinning = true
                            val spins = 8
                            for (i in 0 until spins) {
                                currentReference = generateUniformReference(selectedFilter, selectedScheme)
                                val delayMs = 100L + (i * i * 10L)
                                delay(delayMs.milliseconds)
                            }
                            isSpinning = false
                        }
                    }
                },
                onOpenRequest = { showAppChooser = true }
            )
        }
    }
}

/**
 * Dropdown menu for selecting the Bible book filter.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDropdown(
    selectedFilter: FilterType,
    filterOptions: List<FilterType>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onFilterSelected: (FilterType) -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange
    ) {
        OutlinedTextField(
            value = stringResource(selectedFilter.labelResId),
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            filterOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(stringResource(option.labelResId)) },
                    onClick = {
                        onFilterSelected(option)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}

/**
 * Display box for the current Bible reference with a smooth transition animation.
 */
@Composable
fun ReferenceDisplay(resultText: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(4.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = resultText,
            transitionSpec = {
                (slideInVertically { height -> height } + fadeIn()) togetherWith
                        (slideOutVertically { height -> -height } + fadeOut())
            },
            contentAlignment = Alignment.Center,
            label = "referenceAnimation"
        ) { targetText ->
            Text(
                text = targetText,
                fontSize = 20.sp,
                fontFamily = FontFamily.Serif,
                textAlign = TextAlign.Center,
                lineHeight = 28.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Group of buttons for generating, opening, and copying Bible references.
 */
@Composable
fun ReferenceActionButtons(
    context: Context,
    currentReference: BibleReference?,
    isSpinning: Boolean,
    onGenerate: () -> Unit,
    onOpenRequest: () -> Unit
) {
    BibleAppButton(
        text = stringResource(R.string.generate_reference),
        enabled = !isSpinning,
        onClick = onGenerate
    )

    currentReference?.let { reference ->
        val chapterIsNumeric = reference.chapter.toIntOrNull() != null
        val isCatholicEstherAddition = reference.book == "Esther" && !chapterIsNumeric

        Spacer(modifier = Modifier.height(12.dp))
        when {
            chapterIsNumeric -> {
                BibleAppButton(
                    text = stringResource(R.string.open_reference),
                    enabled = !isSpinning,
                    onClick = onOpenRequest
                )
            }
            isCatholicEstherAddition -> {
                BibleAppButton(
                    text = stringResource(R.string.open_in_biblegateway_nabre),
                    enabled = !isSpinning,
                    onClick = {
                        openCatholicEstherAdditionInBibleGateway(context, reference)
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        BibleAppButton(
            text = stringResource(R.string.copy_reference),
            enabled = !isSpinning,
            onClick = {
                val clipboard = context.getSystemService(ClipboardManager::class.java)
                val clip = ClipData.newPlainText("Bible Reference", reference.toString())
                clipboard.setPrimaryClip(clip)
            }
        )
    }
}

/**
 * Dialog for choosing an external app or website to open the reference.
 */
@Composable
fun AppChooserDialog(
    context: Context,
    currentReference: BibleReference,
    onDismiss: () -> Unit
) {
    val installedApps = remember(currentReference) { getInstalledBibleApps(context) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.open_reference_in)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                AppOptionItem(
                    label = stringResource(R.string.bible_gateway_web),
                    onClick = {
                        openInSpecificApp(context, currentReference, "BibleGateway")
                        onDismiss()
                    }
                )

                installedApps.forEach { app ->
                    AppOptionItem(
                        label = app.label,
                        onClick = {
                            openInSpecificApp(context, currentReference, app.packageName)
                            onDismiss()
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

/**
 * Dialog for selecting the versification scheme.
 */
@Composable
fun SettingsDialog(
    currentScheme: VersificationScheme,
    onSchemeSelected: (VersificationScheme) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings)) },
        text = {
            Column(Modifier.selectableGroup()) {
                Text(
                    text = stringResource(R.string.versification_scheme),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                VersificationScheme.entries.forEach { scheme ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .selectable(
                                selected = (scheme == currentScheme),
                                onClick = { onSchemeSelected(scheme) },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (scheme == currentScheme),
                            onClick = null
                        )
                        Text(
                            text = scheme.label,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

@Composable
fun BibleAppButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
        ),
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text = text, fontSize = 16.sp)
    }
}

@Composable
fun AppOptionItem(label: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

private data class BibleAppInfo(val label: String, val packageName: String)

/**
 * Checks which of the supported Bible apps are currently installed on the device.
 */
private fun getInstalledBibleApps(context: Context): List<BibleAppInfo> {
    val pm = context.packageManager
    val apps = listOf(
        BibleAppInfo("YouVersion", "com.sirma.mobile.bible.android"),
        BibleAppInfo("AndBible", "net.bible.android"),
        BibleAppInfo("AndBible", "net.bible.android.activity"),
        BibleAppInfo("ESV Bible", "com.subsplash.esv"),
        BibleAppInfo("Olive Tree", "biblereader.olivetree"),
        BibleAppInfo("Logos", "com.logos.androidlogos"),
        // BibleAppInfo("Sword Project", "org.crosswire.bishop"),
        BibleAppInfo("Metanoia", "com.bytecats.metanoia"),
        BibleAppInfo("Life Bible", "com.tecarta.TecartaBible"),
        // BibleAppInfo("Bible Gateway (App)", "com.csnmedia.android.bg"),
        BibleAppInfo("Blue Letter Bible", "org.blueletterbible.blb")
    )

    return apps.filter { app ->
        try {
            pm.getPackageInfo(app.packageName, 0)
            true
        } catch (_: Exception) {
            false
        }
    }
}

/**
 * Maps Catholic (NABRE) Esther Addition letter to the logical host chapter in BibleGateway.
 */
private val catholicEstherHostChapter = mapOf(
    "A" to 1,
    "B" to 3,
    "C" to 4,
    "D" to 5,
    "E" to 8,
    "F" to 10
)

/**
 * Opens Catholic-specific additions to Esther on BibleGateway.
 */
private fun openCatholicEstherAdditionInBibleGateway(context: Context, reference: BibleReference) {
    val hostChapter = catholicEstherHostChapter[reference.chapter] ?: 1
    val uri = "https://www.biblegateway.com/passage/?search=Esther+$hostChapter&version=NABRE".toUri()
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        android.util.Log.e("BibleApp", "Failed to open BibleGateway for Esther addition")
    }
}

/**
 * Opens a standard Bible reference in a specific external app or website fallback.
 */
private fun openInSpecificApp(context: Context, reference: BibleReference, target: String) {
    val usfmCode = BibleData.bookAbbreviations[reference.book] ?: reference.book.take(3).uppercase()
    val osisCode = BibleData.osisAbbreviations[reference.book] ?: usfmCode

    // Common reference string formats
    val dotRef = "$osisCode.${reference.chapter}.${reference.verse}"
    val plusRef = "${reference.book.replace(" ", "+")}+${reference.chapter}:${reference.verse}"
    val encodedBook = reference.book.replace(" ", "%20")
    val fullDotRef = "$encodedBook.${reference.chapter}.${reference.verse}"
    val metanoiaPath = "${reference.book.replace(" ", "")}/${reference.chapter}/${reference.verse}"

    // Construction of the deep link URI based on the target app's required format
    val uri = when (target) {
        "com.sirma.mobile.bible.android" ->
            "https://www.bible.com/bible/1/$usfmCode.${reference.chapter}.${reference.verse}".toUri()

        "net.bible.android", "net.bible.android.activity" ->
            "https://read.andbible.org/$dotRef".toUri()

        "com.subsplash.esv" ->
            "https://www.esv.org/$plusRef/".toUri()

        "biblereader.olivetree" ->
            "olivetree://bible/$fullDotRef".toUri()

        "com.logos.androidlogos" ->
            "https://ref.ly/$osisCode${reference.chapter}.${reference.verse}".toUri()

        /* "org.crosswire.bishop" ->
            "bible://$dotRef".toUri() */

        "com.bytecats.metanoia" ->
            "metanoia://bible/$metanoiaPath".toUri()

        "com.tecarta.TecartaBible" ->
            "https://tecartabible.com/bible/$plusRef".toUri()

        /* "com.csnmedia.android.bg" ->
            "https://www.biblegateway.com/passage/?search=$plusRef&version=KJV".toUri() */

        "org.blueletterbible.blb" ->
            "blb://bible/${reference.book.replace(" ", "")}/${reference.chapter}/${reference.verse}".toUri()

        else -> // BibleGateway (Web)
            "https://www.biblegateway.com/passage/?search=$plusRef&version=KJV".toUri()
    }

    // Try opening specifically in the targeted app using its package name
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        if (target != "BibleGateway") {
            setPackage(target)
        }
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    try {
        android.util.Log.d("BibleApp", "Launching deep link for $target: $uri")
        context.startActivity(intent)
    } catch (e: Exception) {
        android.util.Log.e("BibleApp", "Targeted launch failed for $target: ${e.message}")

        // Fallback: If targeted app fails, try opening the URI in the system browser
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            android.util.Log.d("BibleApp", "Falling back to browser for URI: $uri")
            context.startActivity(browserIntent)
        } catch (e2: Exception) {
            // Ultimate fallback to BibleGateway if even the browser can't handle the URI
            android.util.Log.e("BibleApp", "Browser fallback failed for $target: ${e2.message}")
            val gatewayIntent = Intent(Intent.ACTION_VIEW, "https://www.biblegateway.com/passage/?search=$plusRef&version=KJV".toUri()).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(gatewayIntent)
        }
    }
}

/**
 * Data structure representing a potential verse slot (book + chapter combination).
 */
private data class VerseSlot(val book: String, val chapterLabel: String, val maxVerse: Int)

/**
 * Generates a random Bible reference based on the current filter and versification scheme.
 * Ensures uniform distribution across all verses in the selected canon.
 */
fun generateUniformReference(filter: FilterType, scheme: VersificationScheme): BibleReference? {
    val apocryphaForScheme: Map<String, List<Int>> = when (scheme) {
        VersificationScheme.CATHOLIC -> BibleData.catholicApocrypha
        VersificationScheme.ORTHODOX -> BibleData.catholicApocrypha + BibleData.russianOrthodoxExtras
        else -> emptyMap()
    }

    val activeData = mutableMapOf<String, List<Int>>()
    when (filter) {
        FilterType.ALL -> activeData.putAll(BibleData.getLibrary(scheme))
        FilterType.OLD_TESTAMENT -> {
            activeData.putAll(BibleData.oldTestament)
            activeData.putAll(apocryphaForScheme)
        }
        FilterType.NEW_TESTAMENT -> {
            if (scheme != VersificationScheme.HEBREW) {
                activeData.putAll(BibleData.newTestament)
            }
        }
        FilterType.APOCRYPHA -> activeData.putAll(apocryphaForScheme)
    }

    // Extract labeled books (like Catholic Esther) to handle them correctly
    val labeledBooks = activeData.keys.mapNotNull { book ->
        BibleData.getLabeledChapters(scheme, book)?.let { book to it }
    }.toMap()
    labeledBooks.keys.forEach { activeData.remove(it) }

    // Create a flat list of all available verse slots
    val slots = mutableListOf<VerseSlot>()
    for ((book, chapters) in activeData) {
        chapters.forEachIndexed { chIdx, verseCount ->
            slots.add(VerseSlot(book, (chIdx + 1).toString(), verseCount))
        }
    }
    for ((book, labeledChapters) in labeledBooks) {
        labeledChapters.forEach { labeled ->
            slots.add(VerseSlot(book, labeled.label, labeled.maxVerse))
        }
    }

    val totalVerses = slots.sumOf { it.maxVerse }
    if (totalVerses == 0) return null

    // Pick a random verse index and find which slot it belongs to
    val randomVerseIndex = Random.nextInt(1, totalVerses + 1)

    var currentCount = 0
    for (slot in slots) {
        if (currentCount + slot.maxVerse >= randomVerseIndex) {
            val targetVerse = randomVerseIndex - currentCount
            return BibleReference(slot.book, slot.chapterLabel, targetVerse)
        }
        currentCount += slot.maxVerse
    }

    return null
}

/**
 * Preview function for the BibleReferenceScreen.
 */
@Preview(showBackground = true)
@Composable
fun BibleReferenceScreenPreview() {
    BibleAppTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            BibleReferenceScreen()
        }
    }
}
