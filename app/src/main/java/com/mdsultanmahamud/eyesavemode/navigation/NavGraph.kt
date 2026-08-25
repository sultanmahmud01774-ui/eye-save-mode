package com.mdsultanmahamud.eyesavemode.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mdsultanmahamud.eyesavemode.ui.screens.AboutScreen
import com.mdsultanmahamud.eyesavemode.ui.screens.ColorStudioScreen
import com.mdsultanmahamud.eyesavemode.ui.screens.DashboardScreen
import com.mdsultanmahamud.eyesavemode.ui.screens.ScheduleScreen
import com.mdsultanmahamud.eyesavemode.ui.screens.SettingsScreen
import com.mdsultanmahamud.eyesavemode.ui.screens.SultanToolsScreen
import com.mdsultanmahamud.eyesavemode.viewmodel.EyeSaveViewModel

object Routes {
    const val DASHBOARD = "dashboard"
    const val COLOR_STUDIO = "color_studio"
    const val SCHEDULES = "schedules"
    const val SULTAN_TOOLS = "sultan_tools"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
}

@Composable
fun EyeSaveNavGraph(
    navController: NavHostController,
    viewModel: EyeSaveViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.DASHBOARD,
        modifier = modifier.fillMaxSize()
    ) {
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToColorStudio = { navController.navigate(Routes.COLOR_STUDIO) },
                onNavigateToSchedules = { navController.navigate(Routes.SCHEDULES) },
                onNavigateToTools = { navController.navigate(Routes.SULTAN_TOOLS) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateToAbout = { navController.navigate(Routes.ABOUT) }
            )
        }

        composable(Routes.COLOR_STUDIO) {
            ColorStudioScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SCHEDULES) {
            ScheduleScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SULTAN_TOOLS) {
            SultanToolsScreen(
                viewModel = viewModel,
                onNavigateToColorStudio = { navController.navigate(Routes.COLOR_STUDIO) },
                onNavigateToSchedules = { navController.navigate(Routes.SCHEDULES) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ABOUT) {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
