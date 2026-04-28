package com.example.apiarymanager.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.apiarymanager.core.security.PinManager
import com.example.apiarymanager.presentation.navigation.AppDrawerContent
import com.example.apiarymanager.presentation.navigation.AppNavGraph
import com.example.apiarymanager.presentation.navigation.HiveLocationRoute
import com.example.apiarymanager.presentation.navigation.HivesMapRoute
import com.example.apiarymanager.presentation.navigation.LoginRoute
import com.example.apiarymanager.presentation.theme.ApiaryManagerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var pinManager: PinManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by pinManager.isDarkModeFlow.collectAsStateWithLifecycle()
            ApiaryManagerTheme(darkTheme = isDarkMode, dynamicColor = false) {
                val navController  = rememberNavController()
                val drawerState    = rememberDrawerState(DrawerValue.Closed)
                val scope          = rememberCoroutineScope()
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStackEntry?.destination?.route
                val isMapScreen = currentRoute?.let {
                    it.contains(HivesMapRoute::class.qualifiedName ?: "") ||
                    it.contains(HiveLocationRoute::class.qualifiedName ?: "")
                } ?: false

                ModalNavigationDrawer(
                    drawerState      = drawerState,
                    gesturesEnabled  = !isMapScreen,
                    drawerContent    = {
                        AppDrawerContent(
                            currentRoute = currentRoute,
                            onNavigate   = { route ->
                                scope.launch { drawerState.close() }
                                navController.navigate(route) {
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            },
                            onLogout = {
                                scope.launch { drawerState.close() }
                                pinManager.isOnboardingDone = false
                                navController.navigate(LoginRoute) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }
                ) {
                    AppNavGraph(
                        navController = navController,
                        onOpenDrawer  = { scope.launch { drawerState.open() } }
                    )
                }
            }
        }
    }
}
