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
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kubot.monhunsetselector.auth.AuthManager
import com.kubot.monhunsetselector.ui.screens.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.kubot.monhunsetselector.auth.AuthState
import com.kubot.monhunsetselector.data.models.ArmorPiece
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Import your screen classes and placeholder composables

@Composable
fun AppNavigation(authManager: AuthManager) {
    val navController = rememberNavController()

//    val authManager = remember { AuthManager() }
    val authState by authManager.authState.collectAsState() // Observe the new state

//    val user by authManager.currentUser.collectAsState()
    // We now use a different state holder to avoid the initial null problem.
    val userState by authManager.currentUser.collectAsState()
//    val startDestination = if (user != null) Screen.MySets.route else Screen.Login.route

    // State for the drawer (open/closed)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    // A coroutine scope to control the drawer
    val scope = rememberCoroutineScope()

//    // This effect will react to logout and navigate the user away
//    LaunchedEffect(user) {
//        if (user == null) {
//            // Clear the entire back stack and navigate to login
//            navController.navigate(Screen.Login.route) {
//                popUpTo(0)
//            }
//        }
//    }
    // List of screens to show in the drawer
    val drawerItems = listOf(Screen.MySets, Screen.Browse, Screen.NewSetBuilder)
    // Get the current route to highlight the selected item in the drawer
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                // Add your drawer content here
                Spacer(Modifier.height(12.dp))
                drawerItems.forEach { screen ->
                    NavigationDrawerItem(
                        icon = { /* Add icons later if you wish */ },
                        label = { Text(screen.title) },
                        selected = when (screen) {
                            is Screen.Browse -> currentRoute?.startsWith("browse") == true
                            else -> currentRoute == screen.route
                        },
                        onClick = {
                            scope.launch { drawerState.close() }
                            // When navigating to Browse from the drawer, use the no-arg createRoute
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
                // This effect now reacts to the definitive authState
                LaunchedEffect(authState) {
//                    delay(500L)
                    when (authState) {
                        AuthState.AUTHENTICATED -> {
                            // User is logged in, navigate to MySets
                            navController.navigate(Screen.MySets.route) {
                                popUpTo(Screen.Loading.route) { inclusive = true }
                            }
                        }
                        AuthState.UNAUTHENTICATED -> {
                            // We know for sure the user is not logged in, go to Login
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Loading.route) { inclusive = true }
                            }
                        }
                        AuthState.UNKNOWN -> {
                            // Do nothing, just keep showing the loading screen
                        }
                    }
                }
                LoadingScreen()
            }

            // Define Login and MySets routes here. They no longer need complex redirection logic.
            composable(Screen.Login.route) {
                LaunchedEffect(authState) {
                    // If the user becomes authenticated while this screen is visible...
                    if (authState == AuthState.AUTHENTICATED) {
                        // ...navigate them away to the main part of the app.
                        navController.navigate(Screen.MySets.route) {
                            // Clear the back stack so they can't go "back" to the login screen.
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
                    // This is a safety net in case they get here while logged out.
                    // It will redirect them back through the loading->login flow.
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Loading.route) {
                            popUpTo(0)
                        }
                    }
                }
            }

//            composable(route = Screen.Login.route) {
//                if (user != null) {
//                    navController.navigate(Screen.MySets.route) {
//                        popUpTo(Screen.Login.route) { inclusive = true }
//                    }
//                } else {
//                    LoginScreen(navController, authManager)
//                }
//            }

//            composable(route = Screen.MySets.route) {
//                // If the user is somehow null here, redirect to login.
//                // Otherwise, show the MySetsScreen and pass the user data.
//                val currentUser = user
//                if (currentUser != null) {
//                    MySetsScreen(
//                        navController = navController,
//                        user = currentUser,
//                        authManager = authManager,
//                        onMenuClick = { scope.launch { drawerState.open() } }
//                    )
//                }
//            }

            composable(
                route = Screen.Browse.route,
                arguments = listOf(
                    // Define the arguments we expect. They are nullable strings.
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
                        defaultValue = false // Default to NOT being in selection mode
                    }
                )
            ) { backStackEntry ->
                // Extract the arguments from the backStackEntry
                val mainCat = backStackEntry.arguments?.getString("mainCat")
                val subCat = backStackEntry.arguments?.getString("subCat")
                val selectionMode = backStackEntry.arguments?.getBoolean("selectionMode") ?: false
                BrowseScreen(
                    navController = navController,
                    onMenuClick = { scope.launch { drawerState.open() } },
//                    onEquipmentSelected = { equipment:Any ->
//                        // Set the result in the previous screen's state handle
//                        if (equipment is ArmorPiece) {
//                            println("DEBUG_NAV: Equipment selected. Name: '${equipment.name}', Type: '${equipment.type}'")}
//                        navController.previousBackStackEntry
//                            ?.savedStateHandle
//                            ?.set("selected_equipment", equipment)
//                        navController.popBackStack()
//                    },
                    onEquipmentSelected = { selectedEquipment ->
                        val previousHandle = navController.previousBackStackEntry?.savedStateHandle

                        // Set the equipment result
                        previousHandle?.set("selected_equipment", selectedEquipment)

                        // --- THE KEY CHANGE ---
                        // Get the current counter value (defaulting to 0) and increment it.
                        // This 'nonce' (number used once) will force our LaunchedEffect to re-run.
                        val currentNonce = previousHandle?.get<Int>("result_nonce") ?: 0
                        previousHandle?.set("result_nonce", currentNonce + 1)

                        navController.popBackStack()
                    },
                    initialMainCategory = mainCat,
                    initialSubCategory = subCat,
                    isSelectionMode = selectionMode
                )
            }

            // Route for editing an existing set
            composable(
                route = Screen.SetBuilder.route, // "set_builder/{setId}"
                arguments = listOf(navArgument("setId") { type = NavType.StringType; nullable = true })
            ) { backStackEntry ->
                SetBuilderScreen(
                    navController = navController,
                    setId = backStackEntry.arguments?.getString("setId"),
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            }

            // Route for creating a new set
            composable(route = Screen.NewSetBuilder.route) {
                SetBuilderScreen(
                    navController,
                    null,
                    onMenuClick = { scope.launch { drawerState.open() } }
                    ) // Pass null for a new set
            }
        }
    }
}