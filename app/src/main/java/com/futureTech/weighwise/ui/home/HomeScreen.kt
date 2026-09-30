package com.futureTech.weighwise.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.futureTech.weighwise.data.DecisionEntity
import com.futureTech.weighwise.ui.components.BalanceHero

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNewDecision: () -> Unit,
    onDecisionClick: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val decisions by viewModel.decisions.collectAsState()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewDecision,
                icon = { Icon(Icons.Default.Add, contentDescription = "New Decision") },
                text = { Text("New Decision") }
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp, bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "WeighWise",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Find your balance.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    BalanceHero(tiltValue = 0f)
                }
            }

            if (decisions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No decisions yet. Create one to get started.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(decisions) { decision ->
                    DecisionCard(
                        decision = decision,
                        onClick = { onDecisionClick(decision.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun DecisionCard(decision: DecisionEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(decision.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(decision.category, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}
