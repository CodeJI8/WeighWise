package com.futureTech.weighwise.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.futureTech.weighwise.theme.Sora
import com.futureTech.weighwise.theme.SystemSans
import com.futureTech.weighwise.theme.WeighWiseTheme
import com.futureTech.weighwise.ui.components.DecisionCard
import com.futureTech.weighwise.ui.components.HeroCard
import com.futureTech.weighwise.ui.components.PrimaryButton
import com.futureTech.weighwise.ui.components.SegmentedTabs
import java.util.Calendar

private fun getTimeOfDayGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }
}

@Composable
fun HomeScreen(
    onNewDecision: () -> Unit,
    onDecisionClick: (Long) -> Unit,
    onOpenJournal: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeScreenContent(
        uiState = uiState,
        onTabSelected = { viewModel.setTab(it) },
        onNewDecision = onNewDecision,
        onDecisionClick = onDecisionClick,
        onOpenJournal = onOpenJournal
    )
}

@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    onTabSelected: (Int) -> Unit,
    onNewDecision: () -> Unit,
    onDecisionClick: (Long) -> Unit,
    onOpenJournal: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                PrimaryButton(
                    text = "New decision",
                    onClick = onNewDecision
                )
            }
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            val currentDecisions = if (uiState.selectedTab == 0) uiState.decidingDecisions else uiState.decidedDecisions

            LazyColumn(
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + 20.dp,
                    bottom = innerPadding.calculateBottomPadding() + 16.dp,
                    start = 20.dp,
                    end = 20.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Header (Greeting, Title & Journal Button)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = getTimeOfDayGreeting(),
                                fontFamily = SystemSans,
                                fontWeight = FontWeight.Normal,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Your decisions",
                                fontFamily = Sora,
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        IconButton(onClick = onOpenJournal) {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = "Decision Journal",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }

                // Hero Card
                item {
                    HeroCard(
                        title = "Open right now",
                        bigNumber = "${uiState.openCount}",
                        subtitle = uiState.heroSubtitle,
                        balanceTilt = if (uiState.openCount > 0) 0.35f else 0f
                    )
                }

                // Segmented Tabs ("Deciding" vs "Decided")
                item {
                    SegmentedTabs(
                        tabs = listOf("Deciding", "Decided"),
                        selectedIndex = uiState.selectedTab,
                        onTabSelected = onTabSelected
                    )
                }

                // Decision List or Empty State
                if (currentDecisions.isEmpty()) {
                    item {
                        EmptyStateCard(
                            selectedTab = uiState.selectedTab,
                            onNewDecision = onNewDecision
                        )
                    }
                } else {
                    items(
                        items = currentDecisions,
                        key = { it.decision.id }
                    ) { itemState ->
                        DecisionCard(
                            title = itemState.decision.title,
                            category = itemState.decision.category,
                            criteriaCount = itemState.criteriaCount,
                            optionColors = itemState.optionColors,
                            confidenceLevel = itemState.confidenceLevel,
                            confidenceLabel = itemState.confidenceLabel,
                            onClick = { onDecisionClick(itemState.decision.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    selectedTab: Int,
    onNewDecision: () -> Unit,
    modifier: Modifier = Modifier
) {
    val message = if (selectedTab == 0) {
        "No open decisions right now. Ready to weigh your choices?"
    } else {
        "No decided outcomes saved yet."
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = message,
                fontFamily = SystemSans,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (selectedTab == 0) {
                PrimaryButton(
                    text = "New decision",
                    onClick = onNewDecision,
                    modifier = Modifier.width(200.dp)
                )
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true, backgroundColor = 0xFFFAF7F2)
@Preview(name = "Dark Mode", showBackground = true, backgroundColor = 0xFF0A1226, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenPreview() {
    WeighWiseTheme {
        HomeScreenContent(
            uiState = HomeUiState(
                isLoading = false,
                selectedTab = 0,
                openCount = 2,
                clearWinCount = 1,
                closeCallCount = 1,
                heroSubtitle = "1 clear win, 1 close call",
                decidingDecisions = listOf()
            ),
            onTabSelected = {},
            onNewDecision = {},
            onDecisionClick = {},
            onOpenJournal = {}
        )
    }
}
