package com.sreeman.mywifi.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.SignalWifi4Bar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sreeman.mywifi.data.model.SessionState
import com.sreeman.mywifi.ui.auth.LoginScreen
import com.sreeman.mywifi.ui.auth.LoginViewModel
import com.sreeman.mywifi.ui.dashboard.DashboardScreen
import com.sreeman.mywifi.ui.dashboard.DashboardViewModel
import com.sreeman.mywifi.ui.devices.ConnectedDevicesScreen
import com.sreeman.mywifi.ui.wifi.WifiManagementScreen

sealed class NavRoute(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : NavRoute("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Devices : NavRoute("devices", "Devices", Icons.Default.Devices)
    object Wifi : NavRoute("wifi", "Wi-Fi Config", Icons.Default.SignalWifi4Bar)
}

@Composable
fun MainScreen() {
    val loginViewModel: LoginViewModel = hiltViewModel()
    val sessionState by loginViewModel.sessionState.collectAsStateWithLifecycle()

    if (sessionState !is SessionState.Authenticated) {
        LoginScreen(
            viewModel = loginViewModel,
            onLoginSuccess = { }
        )
    } else {
        AuthenticatedAppContent()
    }
}

@Composable
private fun AuthenticatedAppContent() {
    val navController = rememberNavController()
    val dashboardViewModel: DashboardViewModel = hiltViewModel()

    val navItems = listOf(
        NavRoute.Dashboard,
        NavRoute.Devices,
        NavRoute.Wifi
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                navItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavRoute.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavRoute.Dashboard.route) {
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToDevices = { navController.navigate(NavRoute.Devices.route) },
                    onNavigateToWifi = { navController.navigate(NavRoute.Wifi.route) }
                )
            }

            composable(NavRoute.Devices.route) {
                ConnectedDevicesScreen(
                    viewModel = dashboardViewModel
                )
            }

            composable(NavRoute.Wifi.route) {
                WifiManagementScreen(
                    viewModel = dashboardViewModel
                )
            }
        }
    }
}
