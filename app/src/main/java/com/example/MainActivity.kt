package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import com.example.ui.MainTab
import com.example.ui.MainViewModel
import com.example.ui.QuizState
import com.example.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen()
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: MainViewModel = viewModel()) {
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val currentSectionId by viewModel.currentSectionId.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val bookmarkedItems by viewModel.bookmarkedItems.collectAsStateWithLifecycle()
    val quizAttempts by viewModel.quizAttempts.collectAsStateWithLifecycle()
    val quizState by viewModel.quizState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_scaffold"),
        bottomBar = {
            MobileBottomNavigation(
                activeTab = activeTab,
                onTabSelected = { viewModel.setActiveTab(it) }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Info Bar - Luxury Dark Glowing Gradient Title
            AppBarHeader()

            // Main View Selector (Docs with top sliders vs Quiz Panel vs Bookmarked Snippets)
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                label = "MainTabsTransitions"
            ) { tab ->
                when (tab) {
                    MainTab.DOCS -> {
                        DocumentationView(
                            currentSectionId = currentSectionId,
                            searchQuery = searchQuery,
                            bookmarkedItems = bookmarkedItems,
                            onSectionSelected = { viewModel.setSectionId(it) },
                            onSearchQueryChanged = { viewModel.setSearchQuery(it) },
                            onToggleBookmark = { stepTitle -> viewModel.toggleBookmark(stepTitle, currentSectionId) }
                        )
                    }
                    MainTab.QUIZ -> {
                        QuizChallengeView(
                            quizState = quizState,
                            attempts = quizAttempts,
                            onOptionSelected = { viewModel.selectOption(it) },
                            onSubmitAnswer = { viewModel.submitAnswer() },
                            onNextQuestion = { viewModel.nextQuestion() },
                            onRestartQuiz = { viewModel.restartQuiz() },
                            onClearHistory = { viewModel.clearHistory() }
                        )
                    }
                    MainTab.BOOKMARKS -> {
                        SavedCodeSnippetsView(
                            bookmarkedItems = bookmarkedItems,
                            onRemoveBookmark = { stepTitle, secId -> viewModel.toggleBookmark(stepTitle, secId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppBarHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F1B30),
                        Color(0xFF050910)
                    )
                )
            )
            .drawBehind {
                drawLine(
                    color = Color(0xFF1B2E4A),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 2f
                )
            }
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        text = "GS QUANT",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            letterSpacing = 4.sp,
                            color = Color.White
                        )
                    )
                    BadgeWidget(label = "SDK SDK", color = "blue")
                }
                Text(
                    text = "Goldman Sachs Marquee Developer Guide",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            IconButton(
                onClick = { /* Optional decorative metadata info dialog */ },
                modifier = Modifier.testTag("app_info_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = "Terminal Mode Active",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun MobileBottomNavigation(
    activeTab: MainTab,
    onTabSelected: (MainTab) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = Color(0xFF1B2E4A),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.5f
                )
            },
        containerColor = Color(0xFF050910),
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            Triple(MainTab.DOCS, "Docs", Icons.Default.MenuBook),
            Triple(MainTab.QUIZ, "Quiz Challenge", Icons.Default.EmojiEvents),
            Triple(MainTab.BOOKMARKS, "Saved", Icons.Default.Favorite)
        )

        items.forEach { (tab, label, icon) ->
            NavigationBarItem(
                selected = activeTab == tab,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = if (activeTab == tab) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    indicatorColor = Color(0xFF1B2E4A)
                ),
                modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
            )
        }
    }
}

@Composable
fun DocumentationView(
    currentSectionId: String,
    searchQuery: String,
    bookmarkedItems: List<BookmarkEntity>,
    onSectionSelected: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onToggleBookmark: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Search Input Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("search_field"),
            placeholder = {
                Text(
                    text = "Search gs-quant terms, variables, methods...",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChanged("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF0C1322),
                unfocusedContainerColor = Color(0xFF0C1322),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color(0xFF1B2E4A)
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            textStyle = TextStyle(color = Color.White, fontSize = 14.sp)
        )

        if (searchQuery.isNotEmpty()) {
            // Search Results Feed
            SearchResultsList(
                query = searchQuery,
                bookmarkedItems = bookmarkedItems,
                onToggleBookmark = onToggleBookmark,
                onNavigateToSection = { onSectionSelected(it) }
            )
        } else {
            // Standard documentation content with top scrolling pills
            HorizontalCategoryPills(
                currentSectionId = currentSectionId,
                onSectionSelected = onSectionSelected
            )

            val activeSection = GuideData.SECTIONS.firstOrNull { it.id == currentSectionId }
            if (activeSection != null) {
                ActiveSectionContent(
                    section = activeSection,
                    bookmarkedItems = bookmarkedItems,
                    onToggleBookmark = onToggleBookmark
                )
            }
        }
    }
}

@Composable
fun HorizontalCategoryPills(
    currentSectionId: String,
    onSectionSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        GuideData.SECTIONS.forEach { sec ->
            val isSelected = sec.id == currentSectionId
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.secondary else Color(0xFF0C1322))
                    .border(
                        BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF1B2E4A)
                        ),
                        RoundedCornerShape(6.dp)
                    )
                    .clickable { onSectionSelected(sec.id) }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = sec.icon, fontSize = 14.sp)
                Text(
                    text = sec.label,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }
}

@Composable
fun ActiveSectionContent(
    section: GuideSection,
    bookmarkedItems: List<BookmarkEntity>,
    onToggleBookmark: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    // Smooth entry swap animation triggered when section changes
    key(section.id) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Section Title Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = section.icon,
                        fontSize = 24.sp
                    )
                    Text(
                        text = section.title,
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                    )
                }
                BadgeWidget(label = "Apache-2.0", color = "green")
            }

            // Steps implementation
            section.steps.forEachIndexed { index, step ->
                val isBookmarked = bookmarkedItems.any { it.stepTitle == step.label }
                CodeStepCard(
                    stepNum = index + 1,
                    label = step.label,
                    code = step.code,
                    isBookmarked = isBookmarked,
                    onToggleBookmark = { onToggleBookmark(step.label) }
                )
            }

            // Extra details table based on section type
            when (section.id) {
                "credentials" -> {
                    Text(
                        text = "SDK SECURITY SCOPES",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    ScopesTable(scopes = section.scopes)
                }
                "auth" -> {
                    Text(
                        text = "GLOBAL ENVIRONMENTS",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    EnvironmentsTable(envs = section.envs)
                }
                "trade" -> {
                    Text(
                        text = "THE 4-BEAT WORKFLOW",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    FourBeatWorkflow(beats = section.beats)
                }
                "portfolio" -> {
                    Text(
                        text = "COSMIC RISK MEASURES",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    RiskMeasuresCatalog(measures = section.measures)
                }
                "datasets" -> {
                    Text(
                        text = "DATA ENDPOINT METHODS",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    DatasetMethodsTable(methods = section.methods)
                }
                "backtest" -> {
                    Text(
                        text = "BACKTESTING ENGINE TYPES",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    EnginesCatalog(engines = section.engines)
                }
                "troubleshoot" -> {
                    Text(
                        text = "TROUBLESHOOTING COMMON ERRORS",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    TroubleshootTable(errors = section.errors)

                    Text(
                        text = "CRITICAL IMPORT RULES",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    )
                    section.incorrectImports?.let { code ->
                        CodeBlockContainer(codeText = code)
                    }
                }
            }

            // Note block banner
            if (section.note.isNotEmpty()) {
                NoteBanner(note = section.note, isAlert = (section.id == "credentials" || section.id == "troubleshoot"))
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun CodeStepCard(
    stepNum: Int,
    label: String,
    code: String,
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFF1B2E4A)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1322))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "STEP ${stepNum.toString().padStart(2, '0')}",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 2.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    )
                    Text(
                        text = label.uppercase(),
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    )
                }

                IconButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier.testTag("bookmark_$label")
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Toggle Bookmark",
                        tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            CodeBlockContainer(codeText = code)
        }
    }
}

@Composable
fun CodeBlockContainer(codeText: String) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var copiedState by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF04070F))
            .border(BorderStroke(1.dp, Color(0xFF1B2E4A)), RoundedCornerShape(6.dp))
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF070C18))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFFF5F56)))
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFFFBD2E)))
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF27C93F)))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "python python",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    )
                }

                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(codeText))
                        copiedState = true
                        Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .height(28.dp)
                        .testTag("copy_code_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (copiedState) Color(0xFF00D4AA).copy(alpha = 0.2f) else Color(0xFF1B2E4A).copy(alpha = 0.4f)
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(0.5.dp, if (copiedState) Color(0xFF00D4AA) else Color(0xFF1B2E4A))
                ) {
                    Text(
                        text = if (copiedState) "COPIED" else "COPY",
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            letterSpacing = 1.sp,
                            color = if (copiedState) Color(0xFF00D4AA) else MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                LaunchedEffect(copiedState) {
                    if (copiedState) {
                        delay(2000)
                        copiedState = false
                    }
                }
            }

            HorizontalScrollViewContainer {
                Text(
                    text = codeText,
                    modifier = Modifier.padding(14.dp),
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = Color(0xFFC0D2E5)
                    )
                )
            }
        }
    }
}

@Composable
fun HorizontalScrollViewContainer(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        content()
    }
}

@Composable
fun ScopesTable(scopes: List<ScopeItem>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .border(BorderStroke(1.dp, Color(0xFF1B2E4A)), RoundedCornerShape(6.dp)),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        scopes.forEachIndexed { i, s ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (i % 2 == 0) Color(0xFF070C15) else Color(0xFF0B1220))
                    .padding(12.dp)
            ) {
                Text(
                    text = s.scope,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.weight(0.4f)
                )
                Text(
                    text = s.desc,
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.weight(0.6f)
                )
            }
        }
    }
}

@Composable
fun EnvironmentsTable(envs: List<EnvItem>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .border(BorderStroke(1.dp, Color(0xFF1B2E4A)), RoundedCornerShape(6.dp)),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        envs.forEachIndexed { i, e ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (i % 2 == 0) Color(0xFF070C15) else Color(0xFF0B1220))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = e.env,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.weight(0.4f)
                )
                Text(
                    text = e.desc,
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.weight(0.6f)
                )
            }
        }
    }
}

@Composable
fun FourBeatWorkflow(beats: List<BeatItem>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        beats.forEach { beat ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, Color(0xFF1B2E4A)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF080C14))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f), CircleShape)
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.secondary), CircleShape)
                            .size(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✔️",
                            fontSize = 10.sp
                        )
                    }

                    Column {
                        Text(
                            text = beat.step.uppercase(),
                            style = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = beat.call,
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = beat.result,
                            style = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RiskMeasuresCatalog(measures: List<MeasureItem>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .border(BorderStroke(1.dp, Color(0xFF1B2E4A)), RoundedCornerShape(6.dp)),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        // Headers Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F1B30))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = "MEASURE",
                style = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color.White),
                modifier = Modifier.weight(0.4f)
            )
            Text(
                text = "DESCRIPTION",
                style = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color.White),
                modifier = Modifier.weight(0.45f)
            )
            Text(
                text = "CLASS",
                style = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color.White, textAlign = TextAlign.End),
                modifier = Modifier.weight(0.15f)
            )
        }

        measures.forEachIndexed { i, m ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (i % 2 == 0) Color(0xFF070C15) else Color(0xFF0B1220))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = m.measure,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color.White
                    ),
                    modifier = Modifier.weight(0.4f)
                )
                Text(
                    text = m.desc,
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.weight(0.45f)
                )

                Box(
                    modifier = Modifier.weight(0.15f),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    val colorName = when (m.cls) {
                        "All" -> "blue"
                        "Rates" -> "green"
                        "FX" -> "orange"
                        "Equity" -> "red"
                        else -> "blue"
                    }
                    BadgeWidget(label = m.cls, color = colorName)
                }
            }
        }
    }
}

@Composable
fun DatasetMethodsTable(methods: List<MethodItem>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .border(BorderStroke(1.dp, Color(0xFF1B2E4A)), RoundedCornerShape(6.dp)),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        methods.forEachIndexed { i, m ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (i % 2 == 0) Color(0xFF070C15) else Color(0xFF0B1220))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = m.method,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.weight(0.5f)
                )
                Text(
                    text = m.result,
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.weight(0.5f)
                )
            }
        }
    }
}

@Composable
fun EnginesCatalog(engines: List<EngineItem>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        engines.forEach { eng ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, Color(0xFF1B2E4A)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0B111F))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = eng.name,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = eng.desc,
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun TroubleshootTable(errors: List<ErrorItem>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        errors.forEach { err ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, Color(0xFF1B2E4A)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1322))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFF4D4D).copy(alpha = 0.1f), CircleShape)
                                .border(BorderStroke(1.dp, Color(0xFFFF4D4D)), CircleShape)
                                .size(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "❌", fontSize = 8.sp)
                        }
                        Text(
                            text = err.symptom,
                            style = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row {
                        Text(
                            text = "CAUSE: ",
                            style = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFFFFA500)),
                        )
                        Text(
                            text = err.cause,
                            style = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface),
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        Text(
                            text = "FIX: ",
                            style = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF00D4AA)),
                        )
                        Text(
                            text = err.fix,
                            style = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 12.sp, color = Color.White),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NoteBanner(note: String, isAlert: Boolean = false) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (isAlert) Color(0xFFFF4D4D).copy(alpha = 0.08f)
                else MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
            )
            .border(
                border = BorderStroke(
                    1.dp,
                    if (isAlert) Color(0xFFFF4D4D).copy(alpha = 0.4f)
                    else MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(6.dp)
            )
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = if (isAlert) "⚠️" else "ℹ️", fontSize = 14.sp)
            Text(
                text = note,
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 12.sp,
                    color = if (isAlert) Color(0xFFFF9999) else Color(0xFF90C2FF),
                    lineHeight = 18.sp
                )
            )
        }
    }
}

@Composable
fun BadgeWidget(label: String, color: String) {
    val (bgColor, borderColor, textColor) = when (color) {
        "blue" -> Triple(Color(0xFF0074D9).copy(alpha = 0.15f), Color(0xFF0074D9).copy(alpha = 0.5f), Color(0xFF5CB3FF))
        "green" -> Triple(Color(0xFF00D4AA).copy(alpha = 0.15f), Color(0xFF00D4AA).copy(alpha = 0.5f), Color(0xFF00D4AA))
        "orange" -> Triple(Color(0xFFFF851B).copy(alpha = 0.15f), Color(0xFFFF851B).copy(alpha = 0.5f), Color(0xFFFF9A3C))
        "red" -> Triple(Color(0xFFFF4D4D).copy(alpha = 0.15f), Color(0xFFFF4D4D).copy(alpha = 0.5f), Color(0xFFFF6666))
        else -> Triple(Color(0xFF0074D9).copy(alpha = 0.15f), Color(0xFF0074D9).copy(alpha = 0.5f), Color(0xFF5CB3FF))
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(bgColor)
            .border(BorderStroke(0.8.dp, borderColor), RoundedCornerShape(3.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                letterSpacing = 0.5.sp
            )
        )
    }
}

@Composable
fun SearchResultsList(
    query: String,
    bookmarkedItems: List<BookmarkEntity>,
    onToggleBookmark: (String) -> Unit,
    onNavigateToSection: (String) -> Unit
) {
    // Generate filtered matches dynamically from static dataset
    val filteredMatches = remember(query) {
        mutableListOf<SearchMatch>().apply {
            val q = query.lowercase()

            GuideData.SECTIONS.forEach { sec ->
                // Check section title and description match
                if (sec.title.lowercase().contains(q) || sec.label.lowercase().contains(q)) {
                    add(SearchMatch(secId = sec.id, type = "Module", title = sec.title, snippet = "Section Module match"))
                }

                // Check steps list match
                sec.steps.forEach { step ->
                    if (step.label.lowercase().contains(q) || step.code.lowercase().contains(q)) {
                        add(SearchMatch(secId = sec.id, type = "Step Code", title = step.label, snippet = step.code.take(120) + "..."))
                    }
                }

                // Check security scopes
                sec.scopes.forEach { s ->
                    if (s.scope.lowercase().contains(q) || s.desc.lowercase().contains(q)) {
                        add(SearchMatch(secId = sec.id, type = "Scope permission", title = s.scope, snippet = s.desc))
                    }
                }

                // Check error troubleshoot items
                sec.errors.forEach { err ->
                    if (err.symptom.lowercase().contains(q) || err.cause.lowercase().contains(q) || err.fix.lowercase().contains(q)) {
                        add(SearchMatch(secId = sec.id, type = "Troubleshooting Code", title = err.symptom, snippet = "FIX: ${err.fix}"))
                    }
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("search_results"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "${filteredMatches.size} RESULTS FOR '$query'",
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                ),
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        if (filteredMatches.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1322))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "🔍", fontSize = 32.sp)
                        Text(
                            text = "No results found",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Try typing 'IRSwap', 'resolve', 'credentials', or 'auth'",
                            style = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            }
        } else {
            items(filteredMatches) { match ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToSection(match.secId) },
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, Color(0xFF1B2E4A)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1322))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(0.85f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                BadgeWidget(label = match.type.uppercase(), color = "blue")
                                Text(
                                    text = "in ${match.secId.uppercase()}",
                                    style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = MaterialTheme.colorScheme.primary)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = match.title,
                                style = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = match.snippet,
                                style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface),
                                maxLines = 2
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Navigate",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

data class SearchMatch(
    val secId: String,
    val type: String,
    val title: String,
    val snippet: String
)

@Composable
fun QuizChallengeView(
    quizState: QuizState,
    attempts: List<QuizAttemptEntity>,
    onOptionSelected: (Int) -> Unit,
    onSubmitAnswer: () -> Unit,
    onNextQuestion: () -> Unit,
    onRestartQuiz: () -> Unit,
    onClearHistory: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "🏆", fontSize = 24.sp)
                Text(
                    text = "Quiz Challenge",
                    style = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color.White)
                )
            }
            BadgeWidget(label = "KNOWLEDGE EXP", color = "green")
        }

        if (!quizState.isFinished) {
            val qIndex = quizState.currentQuestionIndex
            val question = GuideData.QUIZ_QUESTIONS[qIndex]

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFF1B2E4A)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1322))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Question Progress Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "QUESTION ${qIndex + 1} OF ${GuideData.QUIZ_QUESTIONS.size}",
                            style = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        )
                        Text(
                            text = "SCORE: ${quizState.score}",
                            style = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF00D4AA))
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (qIndex + 1).toFloat() / GuideData.QUIZ_QUESTIONS.size },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = Color(0xFF1B2E4A)
                    )

                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = question.question,
                        style = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Option Buttons
                    question.options.forEachIndexed { optIndex, optionString ->
                        val isSelected = quizState.selectedOptionIndex == optIndex
                        val isCorrectOpt = question.correctIndex == optIndex

                        val borderStroke = when {
                            quizState.isAnswered && isCorrectOpt -> BorderStroke(1.dp, Color(0xFF00D4AA))
                            quizState.isAnswered && isSelected -> BorderStroke(1.dp, Color(0xFFFF4D4D))
                            isSelected -> BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                            else -> BorderStroke(1.dp, Color(0xFF1B2E4A))
                        }

                        val containerColor = when {
                            quizState.isAnswered && isCorrectOpt -> Color(0xFF00D4AA).copy(alpha = 0.12f)
                            quizState.isAnswered && isSelected -> Color(0xFFFF4D4D).copy(alpha = 0.12f)
                            isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            else -> Color(0xFF04070F)
                        }

                        val optionPrefix = ('A' + optIndex).toString()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(containerColor)
                                .border(borderStroke, RoundedCornerShape(6.dp))
                                .clickable(enabled = !quizState.isAnswered) { onOptionSelected(optIndex) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "[$optionPrefix] ",
                                style = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White)
                            )
                            Text(
                                text = optionString,
                                style = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 13.sp, color = Color.White)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (quizState.isAnswered) {
                        // Explanatory Banner
                        val currentCorrection = quizState.selectedOptionIndex == question.correctIndex
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (currentCorrection) Color(0xFF00D4AA).copy(alpha = 0.08f) else Color(0xFFFF4D4D).copy(alpha = 0.08f))
                                .border(BorderStroke(1.dp, if (currentCorrection) Color(0xFF00D4AA).copy(alpha = 0.3f) else Color(0xFFFF4D4D).copy(alpha = 0.3f)), RoundedCornerShape(6.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = if (currentCorrection) "✓ CORRECT ANSWER" else "✗ INCORRECT RESPONSE",
                                    style = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (currentCorrection) Color(0xFF00D4AA) else Color(0xFFFF5F56))
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = question.explanation,
                                    style = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onNextQuestion,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("quiz_next_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(
                                text = if (qIndex + 1 == GuideData.QUIZ_QUESTIONS.size) "FINISH QUIZ" else "NEXT QUESTION",
                                style = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color(0xFF050910))
                            )
                        }
                    } else {
                        Button(
                            onClick = onSubmitAnswer,
                            enabled = quizState.selectedOptionIndex != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("quiz_submit_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text(
                                text = "SUBMIT RESPONSE",
                                style = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color.White)
                            )
                        }
                    }
                }
            }
        } else {
            // Quiz Finish Dashboard
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFF1B2E4A)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1322))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = "🎉", fontSize = 48.sp)
                    Text(
                        text = "COMPLETED SUCCESSFULLY!",
                        style = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        text = "Final Score: ${quizState.score} / ${GuideData.QUIZ_QUESTIONS.size}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )

                    val percentage = (quizState.score.toFloat() / GuideData.QUIZ_QUESTIONS.size * 100).toInt()
                    val feedbackText = when {
                        percentage >= 90 -> "Outstanding! You are ready to price interest rate derivatives at Goldman Sachs."
                        percentage >= 70 -> "Great understanding! Just review portfolios and datasets."
                        else -> "Keep learning! Re-read the steps and try again."
                    }

                    Text(
                        text = feedbackText,
                        style = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onRestartQuiz,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quiz_restart_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = "TAKE QUIZ AGAIN",
                            style = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Color(0xFF050910))
                        )
                    }
                }
            }
        }

        // Historic Accomplishments section
        Text(
            text = "HISTORICAL ATTEMPTS",
            style = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.primary)
        )

        if (attempts.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, Color(0xFF1B2E4A)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0B111F))
            ) {
                Box(
                    modifier = Modifier.padding(18.dp).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No recorded quiz logs yet.",
                        style = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Clear History",
                        style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold),
                        modifier = Modifier
                            .clickable { onClearHistory() }
                            .padding(4.dp)
                    )
                }

                attempts.forEach { att ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, Color(0xFF1B2E4A)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF070B14))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                                val formattedDate = sdf.format(Date(att.timestamp))
                                Text(
                                    text = formattedDate,
                                    style = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                                )
                                val p = (att.score.toFloat() / att.totalQuestions * 100).toInt()
                                Text(
                                    text = "Accuracy: $p%",
                                    style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                )
                            }

                            BadgeWidget(
                                label = "${att.score}/${att.totalQuestions}",
                                color = if (att.score >= 5) "green" else "orange"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SavedCodeSnippetsView(
    bookmarkedItems: List<BookmarkEntity>,
    onRemoveBookmark: (String, String) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "💖", fontSize = 24.sp)
                Text(
                    text = "Saved Code Snippets",
                    style = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color.White)
                )
            }
            BadgeWidget(label = "${bookmarkedItems.size} PLUGINS", color = "blue")
        }

        if (bookmarkedItems.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1322))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "🤍", fontSize = 32.sp)
                    Text(
                        text = "No bookmarks yet",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Click the heart button next to any code step inside standard documentation sections to capture them here.",
                        style = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center),
                        lineHeight = 18.sp
                    )
                }
            }
        } else {
            bookmarkedItems.forEach { bk ->
                // Map to static step details
                val matchingSection = GuideData.SECTIONS.firstOrNull { it.id == bk.sectionId }
                val matchingStep = matchingSection?.steps?.firstOrNull { it.label == bk.stepTitle }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFF1B2E4A)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1322))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "FROM ${bk.sectionId.uppercase()}",
                                    style = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                )
                                Text(
                                    text = bk.stepTitle.uppercase(),
                                    style = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                )
                            }

                            IconButton(
                                onClick = { onRemoveBookmark(bk.stepTitle, bk.sectionId) },
                                modifier = Modifier.testTag("remove_bk_${bk.stepTitle}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Remove Bookmark",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        if (matchingStep != null) {
                            CodeBlockContainer(codeText = matchingStep.code)
                        } else {
                            Text(
                                text = "Could not load saved code content snippet.",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
