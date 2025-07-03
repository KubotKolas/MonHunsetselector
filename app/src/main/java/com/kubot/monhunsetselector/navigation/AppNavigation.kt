package com.kubot.monhunsetselector.navigation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kubot.monhunsetselector.auth.AuthManager
import com.kubot.monhunsetselector.auth.AuthState
import com.kubot.monhunsetselector.ui.screens.BrowseScreen
import com.kubot.monhunsetselector.ui.screens.LoadingScreen
import com.kubot.monhunsetselector.ui.screens.LoginScreen
import com.kubot.monhunsetselector.ui.screens.MySetsScreen
import com.kubot.monhunsetselector.ui.screens.SetBuilderScreen
import kotlinx.coroutines.launch


@Composable
fun AppNavigation(authManager: AuthManager) {
    val navController = rememberNavController()


    val authState by authManager.authState.collectAsState()


    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val scope = rememberCoroutineScope()


    val drawerItems = listOf(Screen.MySets, Screen.Browse, Screen.NewSetBuilder)

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {

                Spacer(Modifier.height(12.dp))
                drawerItems.forEach { screen ->
                    NavigationDrawerItem(
                        icon = {},
                        label = { Text(screen.title) },
                        selected = when (screen) {
                            is Screen.Browse -> currentRoute?.startsWith("browse") == true
                            else -> currentRoute == screen.route
                        },
                        onClick = {
                            scope.launch { drawerState.close() }
                            val route = if (screen is Screen.Browse) {
                                screen.createRoute()
                            } else {
                                screen.route
                            }
                            navController.navigate(route) {
                                launchSingleTop = true
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    )

    {
        NavHost(navController = navController, startDestination = Screen.Loading.route) {

            composable(Screen.Loading.route) {
                LaunchedEffect(authState) {
                    when (authState) {
                        AuthState.AUTHENTICATED -> {
                            navController.navigate(Screen.MySets.route) {
                                popUpTo(Screen.Loading.route) { inclusive = true }
                            }
                        }

                        AuthState.UNAUTHENTICATED -> {

                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Loading.route) { inclusive = true }
                            }
                        }

                        AuthState.UNKNOWN -> {

                        }
                    }
                }
                LoadingScreen()
            }


            composable(Screen.Login.route) {
                LaunchedEffect(authState) {

                    if (authState == AuthState.AUTHENTICATED) {

                        navController.navigate(Screen.MySets.route) {

                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
                LoginScreen(navController, authManager)
            }
            composable(Screen.MySets.route) {
                val user = authManager.currentUser.collectAsState().value
                if (user != null) {
                    MySetsScreen(
                        navController = navController,
                        user = user,
                        authManager = authManager,
                        onMenuClick = { scope.launch { drawerState.open() } }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Loading.route) {
                            popUpTo(0)
                        }
                    }
                }
            }

            composable(
                route = Screen.Browse.route,
                arguments = listOf(

                    navArgument("mainCat") {
                        type = NavType.StringType
                        nullable = true
                    },
                    navArgument("subCat") {
                        type = NavType.StringType
                        nullable = true
                    },
                    navArgument("selectionMode") {
                        type = NavType.BoolType
                        defaultValue = false
                    }
                )
            ) { backStackEntry ->

                val mainCat = backStackEntry.arguments?.getString("mainCat")
                val subCat = backStackEntry.arguments?.getString("subCat")
                val selectionMode = backStackEntry.arguments?.getBoolean("selectionMode") ?: false
                BrowseScreen(
                    navController = navController,
                    onMenuClick = { scope.launch { drawerState.open() } },


                    onEquipmentSelected = { selectedEquipment ->
                        val previousHandle = navController.previousBackStackEntry?.savedStateHandle


                        previousHandle?.set("selected_equipment", selectedEquipment)


                        val currentNonce = previousHandle?.get<Int>("result_nonce") ?: 0
                        previousHandle?.set("result_nonce", currentNonce + 1)

                        navController.popBackStack()
                    },
                    initialMainCategory = mainCat,
                    initialSubCategory = subCat,
                    isSelectionMode = selectionMode
                )
            }


            composable(
                route = Screen.SetBuilder.route,
                arguments = listOf(navArgument("setId") {
                    type = NavType.StringType; nullable = true
                })
            ) { backStackEntry ->
                SetBuilderScreen(
                    navController = navController,
                    setId = backStackEntry.arguments?.getString("setId"),
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            }


            composable(route = Screen.NewSetBuilder.route) {
                SetBuilderScreen(
                    navController,
                    null,
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            }
        }
    }
}