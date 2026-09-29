package com.noboj.weighwise.ui.result

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noboj.weighwise.theme.OptionPalette
import com.noboj.weighwise.ui.components.AnimatedBar
import com.noboj.weighwise.ui.components.BalanceHero

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    decisionId: Long,
    onBack: () -> Unit,
    viewModel: ResultViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.decision?.title ?: "Result") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    IconButton(onClick = {
                        state.decision?.let { dec ->
                            ShareUtils.shareResult(context, dec, state.results)
                        }
                    }) {
                        Icon(androidx.compose.material.icons.Icons.Default.Share, contentDescription = "Share")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                contentPadding = padding,
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
            ) {
                item {
                    val winner = state.results.firstOrNull { !it.isEliminated }
                    val winnerColor = winner?.option?.colorIndex?.let { OptionPalette[it % OptionPalette.size] } 
                        ?: MaterialTheme.colorScheme.primary
                        
                    BalanceHero(tiltValue = 0.8f, rightColor = winnerColor)
                    
                    Spacer(Modifier.height(24.dp))
                    Text("Rankings", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(16.dp))
                }

                items(state.results.size) { i ->
                    val res = state.results[i]
                    val color = OptionPalette[res.option.colorIndex % OptionPalette.size]
                    
                    if (res.isEliminated) {
                        Text("${res.option.name} - Eliminated (${res.eliminationReason})", color = MaterialTheme.colorScheme.error)
                    } else {
                        AnimatedBar(
                            score = res.score,
                            color = color,
                            label = res.option.name,
                            isWinner = i == 0
                        )
                        Spacer(Modifier.height(16.dp))
                    }
                }
                
                item {
                    Spacer(Modifier.height(24.dp))
                    Text("Sensitivity Analysis", style = MaterialTheme.typography.headlineMedium)
                    state.sensitivity.take(3).forEach { sens ->
                        val dir = if (sens.requiredWeightChangePercent > 0) "more" else "less"
                        Text(
                            "${sens.criterion.name} would need to matter ${kotlin.math.abs(sens.requiredWeightChangePercent).toInt()}% $dir for ${sens.flippingOption.name} to win.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    Spacer(Modifier.height(32.dp))
                    if (!state.isDecided) {
                        val winner = state.results.firstOrNull { !it.isEliminated }
                        if (winner != null) {
                            Button(
                                onClick = { viewModel.lockDecision(winner.option.id) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Lock in Decision")
                            }
                        }
                    } else {
                        Text("Decision Locked!", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}
