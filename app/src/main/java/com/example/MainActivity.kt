package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.storage.StorageRepository
import com.example.feature.about.AlphaReleaseNotesScreen
import com.example.feature.calculator.CalculatorScreen
import com.example.feature.calculator.CalculatorViewModel
import com.example.feature.graph.GraphScreen
import com.example.feature.graph.GraphViewModel
import com.example.feature.lessons.LessonsScreen
import com.example.feature.lessons.LessonsViewModel
import com.example.feature.notebook.NotebookScreen
import com.example.feature.notebook.NotebookViewModel
import com.example.ui.theme.ApexBackground
import com.example.ui.theme.ApexCyan
import com.example.ui.theme.ApexCyanContainer
import com.example.ui.theme.ApexMathTheme
import com.example.ui.theme.ApexSurface
import com.example.ui.theme.ApexTextPrimary
import com.example.ui.theme.ApexTextSecondary
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ApexMathTheme {
                ApexMathRoot()
            }
        }
    }
}

private class RepoViewModelFactory<T : ViewModel>(
    private val creator: () -> T
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = creator() as VM
}

@Composable
fun ApexMathRoot() {
    val mainViewModel: MainViewModel = viewModel()
    val repo: StorageRepository = mainViewModel.repository

    val calcViewModel: CalculatorViewModel = viewModel(
        factory = RepoViewModelFactory { CalculatorViewModel(repo) }
    )
    val graphViewModel: GraphViewModel = viewModel(
        factory = RepoViewModelFactory { GraphViewModel(repo) }
    )
    val lessonsViewModel: LessonsViewModel = viewModel(
        factory = RepoViewModelFactory { LessonsViewModel(repo) }
    )
    val notebookViewModel: NotebookViewModel = viewModel(
        factory = RepoViewModelFactory { NotebookViewModel(repo) }
    )

    val selectedTab by mainViewModel.selectedTab.collectAsStateWithLifecycle()
    val showReleaseNotes by mainViewModel.showReleaseNotes.collectAsStateWithLifecycle()
    val pendingOverwrite by mainViewModel.pendingOverwritePrompt.collectAsStateWithLifecycle()
    val settings by mainViewModel.settings.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val pagerState = rememberPagerState(initialPage = 0) { PrimaryTab.entries.size }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        mainViewModel.currentCalculatorExprNonEmpty = { calcViewModel.hasStandardExpression() }
    }

    LaunchedEffect(selectedTab) {
        if (pagerState.currentPage != selectedTab.ordinal) {
            pagerState.animateScrollToPage(selectedTab.ordinal)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        val tab = PrimaryTab.entries[pagerState.currentPage]
        if (selectedTab != tab) mainViewModel.selectTab(tab)
    }

    LaunchedEffect(Unit) {
        mainViewModel.calculatorInjectEvents.collect { (payload, replace) ->
            calcViewModel.applyInjectedPayload(payload.tier, payload.expression, replace)
        }
    }

    LaunchedEffect(Unit) {
        mainViewModel.graphInjectEvents.collect { exprs ->
            graphViewModel.injectExpressions(exprs)
        }
    }

    LaunchedEffect(Unit) {
        mainViewModel.snackbarMessages.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // Back button returns to Calculator from non-Calculator tabs
    BackHandler(enabled = !showReleaseNotes && selectedTab != PrimaryTab.CALCULATOR) {
        mainViewModel.selectTab(PrimaryTab.CALCULATOR)
    }

    if (showReleaseNotes) {
        Scaffold(
            containerColor = ApexBackground,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                AlphaReleaseNotesScreen(
                    settings = settings,
                    onToggleHaptics = { mainViewModel.setHapticEnabled(it) },
                    onBack = { mainViewModel.closeReleaseNotes() }
                )
            }
        }
    } else {
        Scaffold(
            containerColor = ApexBackground,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                NavigationBar(
                    containerColor = ApexSurface,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    PrimaryTab.entries.forEach { tab ->
                        val isSelected = selectedTab == tab
                        val (selectedIcon, unselectedIcon) = when (tab) {
                            PrimaryTab.CALCULATOR -> Icons.Filled.Calculate to Icons.Outlined.Calculate
                            PrimaryTab.LEARN -> Icons.Filled.School to Icons.Outlined.School
                            PrimaryTab.GRAPH -> Icons.Filled.ShowChart to Icons.Outlined.ShowChart
                            PrimaryTab.NOTEBOOK -> Icons.Filled.MenuBook to Icons.Outlined.MenuBook
                        }
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                mainViewModel.selectTab(tab)
                                scope.launch { pagerState.animateScrollToPage(tab.ordinal) }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) selectedIcon else unselectedIcon,
                                    contentDescription = tab.label
                                )
                            },
                            label = { Text(tab.label) },
                            modifier = Modifier.testTag("nav_${tab.name.lowercase()}"),
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ApexCyan,
                                selectedTextColor = ApexCyan,
                                indicatorColor = ApexCyanContainer,
                                unselectedIconColor = ApexTextSecondary,
                                unselectedTextColor = ApexTextSecondary
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            // Disable HorizontalPager swipe on Graph tab so touch-pan & pinch-zoom never conflict
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = selectedTab != PrimaryTab.GRAPH,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) { page ->
                when (PrimaryTab.entries[page]) {
                    PrimaryTab.CALCULATOR -> CalculatorScreen(mainViewModel, calcViewModel)
                    PrimaryTab.LEARN -> LessonsScreen(mainViewModel, lessonsViewModel)
                    PrimaryTab.GRAPH -> GraphScreen(mainViewModel, graphViewModel)
                    PrimaryTab.NOTEBOOK -> NotebookScreen(mainViewModel, notebookViewModel)
                }
            }
        }
    }

    pendingOverwrite?.let { payload ->
        AlertDialog(
            onDismissRequest = { mainViewModel.dismissCalculatorInjection() },
            containerColor = ApexSurface,
            title = { Text("Load Expression into Calculator", color = ApexTextPrimary) },
            text = {
                Text(
                    "Replace your current expression with '${payload.expression}' from ${payload.sourceLabel}, or append it?",
                    color = ApexTextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { mainViewModel.confirmCalculatorInjection(replace = true) }) {
                    Text("Replace", color = ApexCyan)
                }
            },
            dismissButton = {
                RowOrCancelButtons(
                    onAppend = { mainViewModel.confirmCalculatorInjection(replace = false) },
                    onCancel = { mainViewModel.dismissCalculatorInjection() }
                )
            }
        )
    }
}

@Composable
private fun RowOrCancelButtons(onAppend: () -> Unit, onCancel: () -> Unit) {
    androidx.compose.foundation.layout.Row {
        TextButton(onClick = onCancel) { Text("Cancel", color = ApexTextSecondary) }
        TextButton(onClick = onAppend) { Text("Append", color = ApexCyan) }
    }
}
