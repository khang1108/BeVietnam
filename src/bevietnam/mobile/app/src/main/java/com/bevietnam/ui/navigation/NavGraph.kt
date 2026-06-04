package com.bevietnam.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.bevietnam.ui.screens.auth.AuthScreen
import com.bevietnam.ui.screens.profile.ProfileScreen

import com.bevietnam.MainViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bevietnam.ui.components.AppTopBar

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isBottomBarVisible = currentDestination?.let { dest ->
        Screen.bottomNavItems.any { item -> dest.hasRoute(item.route::class) }
    } ?: false

    val currentRouteClass = Screen.bottomNavItems.find { item ->
        currentDestination?.hasRoute(item.route::class) == true
    }?.route?.let { it::class }

    val currentUser by mainViewModel.currentUser.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            if (isBottomBarVisible) {
                AppTopBar(
                    avatarUrl = currentUser?.avatarUrl,
                    onAvatarClick = {
                        navController.navigate(ProfileRoute(currentUser?.id ?: "")) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (isBottomBarVisible) {
                BottomNavBar(
                    currentRouteClass = currentRouteClass,
                    onItemSelected = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = AuthRoute,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable<AuthRoute> {
                AuthScreen(
                    onNavigateToProfile = { userId ->
                        // Navigate to default tab (Feed) instead of Profile 
                        // when login is done, but since Feed isn't ready we keep Profile
                        navController.navigate(ProfileRoute(userId)) {
                            popUpTo<AuthRoute> { inclusive = true }
                        }
                    }
                )
            }

            composable<ExploreRoute> { 
                com.bevietnam.ui.screens.explore.ExploreScreen() 
            }
            
            composable<FeedRoute> { 
                com.bevietnam.ui.screens.feed.FeedScreen() 
            }
            
            composable<StorylineRoute> { 
                com.bevietnam.ui.screens.storyline.StorylineScreen() 
            }
            
            composable<CommunityRoute> { 
                PlaceholderScreen("Cộng đồng") 
            }

            composable<ProfileRoute> {
                ProfileScreen(
                    onNavigateToLogin = {
                        navController.navigate(AuthRoute) {
                            popUpTo(0) // Clear toàn bộ backstack
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Đang phát triển: $title")
    }
}
