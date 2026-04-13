package com.example.apiarymanager.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.apiarymanager.presentation.dashboard.DashboardScreen
import com.example.apiarymanager.presentation.hive.detail.HiveDetailScreen
import com.example.apiarymanager.presentation.hive.list.HiveListScreen
import com.example.apiarymanager.presentation.inspection.InspectionFormScreen
import com.example.apiarymanager.presentation.login.LoginScreen
import com.example.apiarymanager.presentation.register.RegisterScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = LoginRoute,
        modifier = modifier
    ) {
        composable<LoginRoute> {
            LoginScreen(
                onNavigateToDashboard = {
                    navController.navigate(DashboardRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(RegisterRoute)
                }
            )
        }

        composable<RegisterRoute> {
            RegisterScreen(
                onNavigateToDashboard = {
                    navController.navigate(DashboardRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<DashboardRoute> {
            DashboardScreen(
                onNavigateToHiveList = { apiaryId ->
                    navController.navigate(HiveListRoute(apiaryId))
                }
            )
        }

        composable<HiveListRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<HiveListRoute>()
            HiveListScreen(
                apiaryId = route.apiaryId,
                onNavigateToHiveDetail = { hiveId ->
                    navController.navigate(HiveDetailRoute(hiveId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<HiveDetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<HiveDetailRoute>()
            HiveDetailScreen(
                hiveId = route.hiveId,
                onNavigateToInspectionForm = { hiveId, inspectionId ->
                    navController.navigate(InspectionFormRoute(hiveId, inspectionId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<InspectionFormRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<InspectionFormRoute>()
            InspectionFormScreen(
                hiveId = route.hiveId,
                inspectionId = route.inspectionId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
