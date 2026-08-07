package com.example.randombibleverse

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.LabeledIntent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.core.net.toUri
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// --- Reusable Theme Wrapper ---
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BibleReferenceScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
    
    var selectedScheme by remember {
        mutableStateOf(
            try {
                VersificationScheme.valueOf(
                    prefs.getString("versification_scheme", VersificationScheme.KJV.name) 
                        ?: VersificationScheme.KJV.name
                )
            } catch (e: Exception) {
                VersificationScheme.KJV
            }
        )
    }
    
    val filterOptions = remember(selectedScheme) {
        when (selectedScheme) {
            VersificationScheme.KJV, VersificationScheme.NRSV -> 
                listOf("All Books", "Old Testament", "New Testament")
            VersificationScheme.CATHOLIC -> 
                listOf("All Books", "Old Testament", "New Testament", "Apocrypha")
            VersificationScheme.ORTHODOX -> 
                listOf("All Books", "Old Testament", "New Testament")
            VersificationScheme.HEBREW -> 
                listOf("Old Testament")
        }
    }
    
    var expanded by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(filterOptions[0]) }
    
    // Reset filter if it's no longer available for the current scheme
    LaunchedEffect(selectedScheme) {
        if (selectedFilter !in filterOptions) {
            selectedFilter = filterOptions[0]
        }
    }
    
    var currentReference by remember { mutableStateOf<BibleReference?>(null) }
    var isSpinning by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAppChooser by remember { mutableStateOf(false) }

    val resultText = currentReference?.toString() ?: "Click the button below\nto get a reference!"

    if (showAppChooser && currentReference != null) {
        val installedApps = remember(currentReference) { getInstalledBibleApps(context) }
        
        AlertDialog(
            onDismissRequest = { showAppChooser = false },
            title = { Text("Open Reference In...") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    // Always show BibleGateway
                    AppOptionItem(
                        label = "BibleGateway (Web)",
                        onClick = {
                            openInSpecificApp(context, currentReference!!, "BibleGateway")
                            showAppChooser = false
                        }
                    )
                    
                    installedApps.forEach { app ->
                        AppOptionItem(
                            label = app.label,
                            onClick = {
                                openInSpecificApp(context, currentReference!!, app.packageName)
                                showAppChooser = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAppChooser = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Settings") },
            text = {
                Column(Modifier.selectableGroup()) {
                    Text(
                        text = "Versification Scheme",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    VersificationScheme.values().forEach { scheme ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .selectable(
                                    selected = (scheme == selectedScheme),
                                    onClick = {
                                        selectedScheme = scheme
                                        prefs.edit { putString("versification_scheme", scheme.name) }
                                    },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (scheme == selectedScheme),
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
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
            // --- Filter Dropdown ---
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedFilter,
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
                    onDismissRequest = { expanded = false }
                ) {
                    filterOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                selectedFilter = option
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Output Display Box ---
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

            Spacer(modifier = Modifier.height(24.dp))

            // --- Buttons ---
            BibleAppButton(
                text = "Generate Reference",
                enabled = !isSpinning,
                onClick = {
                    if (!isSpinning) {
                        scope.launch {
                            isSpinning = true
                            val spins = 8
                            for (i in 0 until spins) {
                                currentReference = generateUniformReference(selectedFilter, selectedScheme)
                                val delayMs = 100L + (i * i * 10L)
                                delay(delayMs)
                            }
                            isSpinning = false
                        }
                    }
                }
            )

            if (currentReference != null) {
                Spacer(modifier = Modifier.height(12.dp))
                BibleAppButton(
                    text = "Open Reference",
                    enabled = !isSpinning,
                    onClick = {
                        showAppChooser = true
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))
                BibleAppButton(
                    text = "Copy Reference",
                    enabled = !isSpinning,
                    onClick = {
                        val clipboard = context.getSystemService(ClipboardManager::class.java)
                        val clip = ClipData.newPlainText("Bible Reference", currentReference.toString())
                        clipboard.setPrimaryClip(clip)
                    }
                )
            }
        }
    }
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

private fun getInstalledBibleApps(context: Context): List<BibleAppInfo> {
    val pm = context.packageManager
    val apps = listOf(
        BibleAppInfo("YouVersion", "com.sirma.mobile.bible.android"),
        BibleAppInfo("AndBible", "net.bible.android"),
        BibleAppInfo("AndBible", "net.bible.android.activity"),
        BibleAppInfo("ESV Bible", "com.subsplash.esv"),
        BibleAppInfo("Olive Tree", "biblereader.olivetree"),
        BibleAppInfo("Logos", "com.logos.androidlogos"),
        BibleAppInfo("Sword Project", "org.crosswire.bishop"),
        BibleAppInfo("Metanoia", "com.bytecats.metanoia"),
        BibleAppInfo("Life Bible", "com.tecarta.TecartaBible"),
        BibleAppInfo("Bible Gateway (App)", "com.csnmedia.android.bg"),
        BibleAppInfo("Blue Letter Bible", "org.blueletterbible.blb")
    )
    
    return apps.filter { app ->
        try {
            pm.getPackageInfo(app.packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }
}

private fun openInSpecificApp(context: Context, reference: BibleReference, target: String) {
    val usfmCode = BibleData.bookAbbreviations[reference.book] ?: reference.book.take(3).uppercase()
    val osisCode = BibleData.osisAbbreviations[reference.book] ?: usfmCode
    
    // Formatting variations
    val dotRef = "$osisCode.${reference.chapter}.${reference.verse}"
    val plusRef = "${reference.book.replace(" ", "+")}+${reference.chapter}:${reference.verse}"
    val encodedBook = reference.book.replace(" ", "%20")
    val fullDotRef = "$encodedBook.${reference.chapter}.${reference.verse}"
    val metanoiaPath = "${reference.book.replace(" ", "")}/${reference.chapter}/${reference.verse}"

    // Define the primary URI for the chosen service
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
            
        "org.crosswire.bishop" -> 
            "bible://$dotRef".toUri()
            
        "com.bytecats.metanoia" -> 
            "metanoia://bible/$metanoiaPath".toUri()
            
        "com.tecarta.TecartaBible" -> 
            "https://tecartabible.com/bible/$plusRef".toUri()
            
        "com.csnmedia.android.bg" -> 
            "https://www.biblegateway.com/passage/?search=$plusRef&version=KJV".toUri()
            
        "org.blueletterbible.blb" -> 
            "blb://bible/${reference.book.replace(" ", "")}/${reference.chapter}/${reference.verse}".toUri()
            
        else -> // BibleGateway (Web)
            "https://www.biblegateway.com/passage/?search=$plusRef&version=KJV".toUri()
    }

    // Attempt 1: Try to open specifically in the targeted app
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        if (target != "BibleGateway") {
            setPackage(target)
        }
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    try {
        android.util.Log.d("BibleApp", "Attempting to launch $target with URI: $uri")
        context.startActivity(intent)
    } catch (e: Exception) {
        android.util.Log.e("BibleApp", "Targeted launch failed for $target: ${e.message}")
        
        // Attempt 2: If targeted launch fails, try opening the SAME URI in the system browser
        // This breaks the "BibleGateway monopoly" for apps that use web URLs (like ESV)
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            android.util.Log.d("BibleApp", "Falling back to browser for URI: $uri")
            context.startActivity(browserIntent)
        } catch (e2: Exception) {
            // Ultimate fallback to BibleGateway if even the browser can't handle the URI (e.g. it's a dead custom scheme)
            android.util.Log.e("BibleApp", "Browser fallback failed, using BibleGateway")
            val gatewayIntent = Intent(Intent.ACTION_VIEW, "https://www.biblegateway.com/passage/?search=$plusRef&version=KJV".toUri()).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(gatewayIntent)
        }
    }
}

fun generateUniformReference(filter: String, scheme: VersificationScheme): BibleReference? {
    val library = BibleData.getLibrary(scheme)
    val activeData = mutableMapOf<String, List<Int>>()
    
    when (filter) {
        "All Books" -> activeData.putAll(library)
        "Old Testament" -> {
            activeData.putAll(BibleData.oldTestament)
            if (scheme == VersificationScheme.CATHOLIC || scheme == VersificationScheme.ORTHODOX) {
                activeData.putAll(BibleData.apocrypha)
            }
        }
        "New Testament" -> {
            if (scheme != VersificationScheme.HEBREW) {
                activeData.putAll(BibleData.newTestament)
            }
        }
        "Apocrypha" -> activeData.putAll(BibleData.apocrypha)
        "Protestant Canon" -> {
            activeData.putAll(BibleData.oldTestament)
            activeData.putAll(BibleData.newTestament)
        }
    }

    var totalVerses = 0
    for (chapters in activeData.values) {
        totalVerses += chapters.sum()
    }

    if (totalVerses == 0) return null

    val randomVerseIndex = Random.nextInt(1, totalVerses + 1)

    var currentCount = 0
    for ((book, chapters) in activeData) {
        for ((chIdx, verseCount) in chapters.withIndex()) {
            if (currentCount + verseCount >= randomVerseIndex) {
                val targetChapter = chIdx + 1
                val targetVerse = randomVerseIndex - currentCount
                return BibleReference(book, targetChapter, targetVerse)
            }
            currentCount += verseCount
        }
    }

    return null
}

@Preview(showBackground = true)
@Composable
fun BibleReferenceScreenPreview() {
    BibleAppTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            BibleReferenceScreen()
        }
    }
}
