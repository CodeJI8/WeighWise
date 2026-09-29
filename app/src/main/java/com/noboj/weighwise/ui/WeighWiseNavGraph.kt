package com.noboj.weighwise.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.noboj.weighwise.ui.home.HomeScreen
import com.noboj.weighwise.ui.wizard.WizardScreen
import com.noboj.weighwise.ui.result.ResultScreen

@Composable
fun WeighWiseNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(500)) },
        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(500)) },
        popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(500)) },
        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(500)) }
    ) {
        composable("home") {
            HomeScreen(
                onNewDecision = { navController.navigate("wizard") },
                onDecisionClick = { id -> navController.navigate("result/$id") }
            )
        }
        composable("wizard") {
            WizardScreen(
                onFinish = { id -> 
                    navController.navigate("result/$id") {
                        popUpTo("home")
                    }
                },
                onCancel = { navController.popBackStack() }
            )
        }
        composable("result/{decisionId}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("decisionId")?.toLongOrNull() ?: 0L
            ResultScreen(
                decisionId = id,
                onBack = { navController.navigateUp() }
            )
        }
    }
}
