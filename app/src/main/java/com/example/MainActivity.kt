package com.example

import android.os.Bundle
import android.Manifest
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.screens.BillsScreen
import com.example.ui.screens.BudgetsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GoalsScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PinLockScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplitScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.BudgetBuddyTheme
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.SlateCardBg
import com.example.ui.theme.SlateDarkBg
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.FinanceViewModel
import com.example.util.NotificationHelper
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

class MainActivity : ComponentActivity() {

    private val viewModel: FinanceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.createNotificationChannel(this)

        setContent {
            BudgetBuddyTheme {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    RequestNotificationPermission()
                }

                val onboardingCompleted by viewModel.onboardingCompleted.collectAsState()
                val pinLockEnabled by viewModel.pinLockEnabled.collectAsState()
                var isUnlocked by remember { mutableStateOf(false) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(SlateDarkBg)
                            .padding(innerPadding)
                    ) {
                        when {
                            !onboardingCompleted -> {
                                OnboardingScreen(
                                    onGetStartedClick = { viewModel.completeOnboarding() }
                                )
                            }
                            pinLockEnabled && !isUnlocked -> {
                                PinLockScreen(
                                    viewModel = viewModel,
                                    onUnlock = { isUnlocked = true }
                                )
                            }
                            else -> {
                                MainAppLayout(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionState = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
        if (!permissionState.status.isGranted) {
            LaunchedEffect(Unit) {
                permissionState.launchPermissionRequest()
            }
        }
    }
}

@Composable
fun MainAppLayout(viewModel: FinanceViewModel) {
    var activeBottomTab by remember { mutableIntStateOf(0) }

    // Trigger streak check on app load
    LaunchedEffect(Unit) {
        viewModel.updateStreak()
    }

    // Segment tab selections for combined screens
    var activeBudgetSubTab by remember { mutableIntStateOf(0) } // 0 = Budgets, 1 = Goals, 2 = Settings

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = SlateCardBg,
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                // Dashboard
                NavigationBarItem(
                    selected = activeBottomTab == 0,
                    onClick = { activeBottomTab = 0 },
                    icon = { Icon(imageVector = Icons.Default.Dashboard, contentDescription = "Dashboard", modifier = Modifier.size(22.dp)) },
                    label = { Text("Home", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SlateCardBg,
                        selectedTextColor = ElectricTeal,
                        indicatorColor = ElectricTeal,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                // Transactions
                NavigationBarItem(
                    selected = activeBottomTab == 1,
                    onClick = { activeBottomTab = 1 },
                    icon = { Icon(imageVector = Icons.Default.Payments, contentDescription = "Transactions", modifier = Modifier.size(22.dp)) },
                    label = { Text("Ledger", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SlateCardBg,
                        selectedTextColor = ElectricTeal,
                        indicatorColor = ElectricTeal,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                // Budgets & Goals (Targets)
                NavigationBarItem(
                    selected = activeBottomTab == 2,
                    onClick = { activeBottomTab = 2 },
                    icon = { Icon(imageVector = Icons.Default.Bookmark, contentDescription = "Budgets & Goals", modifier = Modifier.size(22.dp)) },
                    label = { Text("Targets", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SlateCardBg,
                        selectedTextColor = ElectricTeal,
                        indicatorColor = ElectricTeal,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                // Bills & Reminders
                NavigationBarItem(
                    selected = activeBottomTab == 3,
                    onClick = { activeBottomTab = 3 },
                    icon = { Icon(imageVector = Icons.Default.EventNote, contentDescription = "Bills", modifier = Modifier.size(22.dp)) },
                    label = { Text("Bills", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SlateCardBg,
                        selectedTextColor = ElectricTeal,
                        indicatorColor = ElectricTeal,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                // Split Tools
                NavigationBarItem(
                    selected = activeBottomTab == 4,
                    onClick = { activeBottomTab = 4 },
                    icon = { Icon(imageVector = Icons.Default.Groups, contentDescription = "Split", modifier = Modifier.size(22.dp)) },
                    label = { Text("Split", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SlateCardBg,
                        selectedTextColor = ElectricTeal,
                        indicatorColor = ElectricTeal,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
            }
        },
        containerColor = SlateDarkBg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeBottomTab) {
                0 -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToTransactions = { activeBottomTab = 1 },
                        onNavigateToBudgets = { activeBottomTab = 2; activeBudgetSubTab = 0 },
                        onQuickAddClick = { activeBottomTab = 1 }
                    )
                }
                1 -> {
                    TransactionsScreen(viewModel = viewModel)
                }
                2 -> {
                    // Combine Budgets, Goals and Settings under a neat tab system
                    Column(modifier = Modifier.fillMaxSize()) {
                        TabRow(
                            selectedTabIndex = activeBudgetSubTab,
                            containerColor = Color.Transparent,
                            contentColor = ElectricTeal,
                            indicator = { tabPositions ->
                                TabRowDefaults.PrimaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeBudgetSubTab]),
                                    color = ElectricTeal
                                )
                            },
                            divider = {}
                        ) {
                            Tab(
                                selected = activeBudgetSubTab == 0,
                                onClick = { activeBudgetSubTab = 0 },
                                text = { Text("Budgets", fontWeight = FontWeight.Bold, color = if (activeBudgetSubTab == 0) ElectricTeal else TextSecondary) }
                            )
                            Tab(
                                selected = activeBudgetSubTab == 1,
                                onClick = { activeBudgetSubTab = 1 },
                                text = { Text("Goals", fontWeight = FontWeight.Bold, color = if (activeBudgetSubTab == 1) ElectricTeal else TextSecondary) }
                            )
                            Tab(
                                selected = activeBudgetSubTab == 2,
                                onClick = { activeBudgetSubTab = 2 },
                                text = { Text("Config", fontWeight = FontWeight.Bold, color = if (activeBudgetSubTab == 2) ElectricTeal else TextSecondary) }
                            )
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            when (activeBudgetSubTab) {
                                0 -> BudgetsScreen(viewModel = viewModel)
                                1 -> GoalsScreen(viewModel = viewModel)
                                2 -> SettingsScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
                3 -> {
                    BillsScreen(viewModel = viewModel)
                }
                4 -> {
                    SplitScreen(viewModel = viewModel)
                }
            }
        }
    }
}
