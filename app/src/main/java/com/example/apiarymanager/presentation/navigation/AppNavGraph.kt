package com.example.apiarymanager.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.apiarymanager.presentation.apiary.ApiaryFormScreen
import com.example.apiarymanager.presentation.apiary.ApiaryListScreen
import com.example.apiarymanager.presentation.auth.forgotpassword.ForgotPasswordScreen
import com.example.apiarymanager.presentation.camera.CameraScreen
import com.example.apiarymanager.presentation.dashboard.DashboardScreen
import com.example.apiarymanager.presentation.feeding.FeedingFormScreen
import com.example.apiarymanager.presentation.harvest.HarvestFormScreen
import com.example.apiarymanager.presentation.hive.detail.HiveDetailScreen
import com.example.apiarymanager.presentation.hive.form.HiveFormScreen
import com.example.apiarymanager.presentation.hive.list.HiveListScreen
import com.example.apiarymanager.presentation.inspection.InspectionFormScreen
import com.example.apiarymanager.presentation.login.LoginScreen
import com.example.apiarymanager.presentation.onboarding.OnboardingCarouselScreen
import com.example.apiarymanager.presentation.onboarding.PinScreen
import com.example.apiarymanager.presentation.register.RegisterScreen
import com.example.apiarymanager.presentation.settings.SettingsScreen
import com.example.apiarymanager.presentation.statistics.StatisticsScreen
import com.example.apiarymanager.presentation.task.TaskFormScreen
import com.example.apiarymanager.presentation.task.TaskListScreen
import com.example.apiarymanager.presentation.treatment.TreatmentFormScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController    = navController,
        startDestination = LoginRoute,
        modifier         = modifier
    ) {

        // ─── Auth ────────────────────────────────────────────────────────────

        composable<LoginRoute> {
            LoginScreen(
                onNavigateToDashboard = {
                    navController.navigate(DashboardRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(RegisterRoute) },
                onNavigateToForgotPassword = { navController.navigate(ForgotPasswordRoute) }
            )
        }

        composable<RegisterRoute> {
            RegisterScreen(
                onNavigateToDashboard = {
                    navController.navigate(DashboardRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<ForgotPasswordRoute> {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ─── Onboarding ──────────────────────────────────────────────────────

        composable<OnboardingCarouselRoute> {
            OnboardingCarouselScreen(
                onFinish = {
                    navController.navigate(OnboardingPinRoute) {
                        popUpTo(OnboardingCarouselRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<OnboardingPinRoute> {
            PinScreen(
                onNavigateToDashboard = {
                    navController.navigate(DashboardRoute) {
                        popUpTo(OnboardingPinRoute) { inclusive = true }
                    }
                }
            )
        }

        // ─── Main app ────────────────────────────────────────────────────────

        composable<DashboardRoute> {
            DashboardScreen(
                onNavigateToHiveList = { apiaryId ->
                    navController.navigate(HiveListRoute(apiaryId))
                },
                onNavigateToApiaryForm = {
                    navController.navigate(ApiaryFormRoute())
                },
                onNavigateToTaskForm = {
                    navController.navigate(TaskFormRoute())
                },
                onNavigateToInspectionForm = { hiveId ->
                    navController.navigate(InspectionFormRoute(hiveId = hiveId))
                },
                onNavigateToHarvestForm = { hiveId ->
                    navController.navigate(HarvestFormRoute(hiveId = hiveId))
                },
                onOpenDrawer = onOpenDrawer
            )
        }

        composable<ApiaryListRoute> {
            ApiaryListScreen(
                onNavigateToHiveList   = { apiaryId -> navController.navigate(HiveListRoute(apiaryId)) },
                onNavigateToApiaryForm = { apiaryId -> navController.navigate(ApiaryFormRoute(apiaryId)) },
                onOpenDrawer           = onOpenDrawer
            )
        }

        composable<TaskListRoute> {
            TaskListScreen(
                onNavigateToTaskForm = { taskId -> navController.navigate(TaskFormRoute(taskId = taskId)) },
                onOpenDrawer         = onOpenDrawer
            )
        }

        composable<ApiaryFormRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ApiaryFormRoute>()
            ApiaryFormScreen(
                apiaryId       = route.apiaryId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<StatisticsRoute> {
            StatisticsScreen(
                onNavigateBack = { navController.popBackStack() },
                onOpenDrawer   = onOpenDrawer
            )
        }

        composable<SettingsRoute> {
            SettingsScreen(
                onNavigateToLogin = {
                    navController.navigate(LoginRoute) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onOpenDrawer = onOpenDrawer
            )
        }

        // ─── Hive list ───────────────────────────────────────────────────────

        composable<HiveListRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<HiveListRoute>()
            HiveListScreen(
                apiaryId = route.apiaryId,
                onNavigateToHiveDetail = { hiveId ->
                    navController.navigate(HiveDetailRoute(hiveId))
                },
                onNavigateToHiveForm = { apiaryId, hiveId ->
                    navController.navigate(HiveFormRoute(apiaryId, hiveId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<HiveFormRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<HiveFormRoute>()
            HiveFormScreen(
                apiaryId       = route.apiaryId,
                hiveId         = route.hiveId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ─── Hive detail ─────────────────────────────────────────────────────

        composable<HiveDetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<HiveDetailRoute>()
            HiveDetailScreen(
                hiveId = route.hiveId,
                initialTab = route.initialTab,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHiveForm = { apiaryId, hiveId ->
                    navController.navigate(HiveFormRoute(apiaryId, hiveId))
                },
                onNavigateToInspectionForm = { hiveId, inspectionId ->
                    navController.navigate(InspectionFormRoute(hiveId, inspectionId))
                },
                onNavigateToHarvestForm = { hiveId, harvestId ->
                    navController.navigate(HarvestFormRoute(hiveId, harvestId))
                },
                onNavigateToTreatmentForm = { hiveId, treatmentId ->
                    navController.navigate(TreatmentFormRoute(hiveId, treatmentId))
                },
                onNavigateToFeedingForm = { hiveId, feedingId ->
                    navController.navigate(FeedingFormRoute(hiveId, feedingId))
                },
                onNavigateToTaskForm = { hiveId, taskId ->
                    navController.navigate(TaskFormRoute(hiveId = hiveId, taskId = taskId))
                }
            )
        }

        // ─── Inspection ──────────────────────────────────────────────────────

        composable<InspectionFormRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<InspectionFormRoute>()
            InspectionFormScreen(
                hiveId       = route.hiveId,
                inspectionId = route.inspectionId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCamera = { outputDir ->
                    navController.navigate(CameraRoute(outputDir))
                }
            )
        }

        // ─── Camera ──────────────────────────────────────────────────────────

        composable<CameraRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<CameraRoute>()
            CameraScreen(
                outputDir = route.outputDir,
                onPhotoTaken = { path ->
                    // Pass result back to InspectionFormScreen via SavedStateHandle
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("capturedPhotoPath", path)
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ─── Activity forms ──────────────────────────────────────────────────

        composable<HarvestFormRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<HarvestFormRoute>()
            HarvestFormScreen(
                hiveId         = route.hiveId,
                harvestId      = route.harvestId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<TreatmentFormRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<TreatmentFormRoute>()
            TreatmentFormScreen(
                hiveId         = route.hiveId,
                treatmentId    = route.treatmentId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<FeedingFormRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<FeedingFormRoute>()
            FeedingFormScreen(
                hiveId         = route.hiveId,
                feedingId      = route.feedingId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ─── Tasks ───────────────────────────────────────────────────────────

        composable<TaskFormRoute> {
            TaskFormScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
