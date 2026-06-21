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
import com.bevietnam.core.model.User
import com.bevietnam.ui.components.AppTopBar
import com.bevietnam.ui.screens.capture.CaptureScreen
import com.bevietnam.ui.screens.explore.ExploreScreen
import com.bevietnam.ui.screens.feed.FeedScreen
import com.bevietnam.ui.screens.storyline.StorylineScreen
import com.bevietnam.ui.screens.place.PlaceDetailScreen
import com.bevietnam.ui.screens.place.PlaceDetailViewModel
import com.bevietnam.ui.screens.food.FoodDetailScreen
import com.bevietnam.ui.screens.story.StoryDetailScreen
import com.bevietnam.ui.content.CultureContent
import androidx.navigation.toRoute
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto

/**
 * Điểm điều hướng chính (Main App Navigation Host) của ứng dụng BeVietnam.
 *
 * Nhận `MainViewModel` ở phạm vi toàn cục để theo dõi thông tin tài khoản người dùng hiện tại,
 * và điều phối luồng giao diện thông qua [AppNavHostContent].
 *
 * @param navController Đối tượng quản lý điều hướng chính của ứng dụng ([NavHostController]).
 * @param modifier [Modifier] dùng để định hình bố cục bên ngoài truyền vào.
 * @param mainViewModel ViewModel toàn cục theo dõi thông tin người dùng ([MainViewModel]). Mặc định là [hiltViewModel].
 */
@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val currentUser by mainViewModel.currentUser.collectAsStateWithLifecycle()

    AppNavHostContent(
        navController = navController,
        currentUser = currentUser,
        modifier = modifier
    )
}

/**
 * Triển khai giao diện Scaffold và cấu hình biểu đồ điều hướng (NavHost) của BeVietnam.
 *
 * Quản lý tự động việc hiển thị/ẩn thanh TopAppBar và BottomNavBar dựa trên tuyến đường (Route) hiện tại,
 * đồng thời cấu hình Backstack điều hướng an toàn và hiệu quả cho các tab.
 *
 * @param navController Đối tượng quản lý điều hướng chính của ứng dụng ([NavHostController]).
 * @param currentUser Đối tượng thông tin người dùng hiện tại đang đăng nhập ([User]).
 * @param modifier [Modifier] dùng để định hình bố cục bên ngoài truyền vào.
 */
@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun AppNavHostContent(
    navController: NavHostController,
    currentUser: User?,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Xác định xem màn hình hiện tại có thuộc nhóm Bottom Navigation Bar hay không
    val isBottomBarVisible = currentDestination?.let { dest ->
        Screen.bottomNavItems.any { item -> dest.hasRoute(item.route::class) }
    } ?: false

    // Auto-login / Auth navigation side effect
    androidx.compose.runtime.LaunchedEffect(currentUser) {
        if (currentUser != null) {
            // Nếu có dữ liệu user nhưng đang ở AuthRoute (Login/Register), đẩy thẳng vào ProfileRoute
            if (currentDestination?.hasRoute(AuthRoute::class) == true || currentDestination == null) {
                navController.navigate(ProfileRoute(currentUser.id)) {
                    popUpTo(AuthRoute) { inclusive = true }
                }
            }
        }
    }

    // Xác định KClass đại diện cho Route của màn hình đang hiển thị để làm nổi bật tab điều hướng tương ứng
    val currentRouteClass = Screen.bottomNavItems.find { item ->
        currentDestination?.hasRoute(item.route::class) == true
    }?.route?.let { it::class }

    Scaffold(
        modifier = modifier,
        topBar = {
            if (isBottomBarVisible) {
                AppTopBar()
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
        },
        floatingActionButton = {
            if (isBottomBarVisible) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(CameraRoute) { launchSingleTop = true }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = "Đăng bài mới")
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = AuthRoute,
            modifier = Modifier.padding(top = paddingValues.calculateTopPadding())
        ) {
            composable<AuthRoute> {
                AuthScreen(
                    onNavigateToProfile = { userId ->
                        navController.navigate(ProfileRoute(userId)) {
                            popUpTo<AuthRoute> { inclusive = true }
                        }
                    }
                )
            }

            composable<ExploreRoute> {
                ExploreScreen(
                    onPlaceClick = { place ->
                        navController.navigate(PlaceDetailRoute(place.id))
                    },
                    onFoodClick = { foodId ->
                        navController.navigate(FoodDetailRoute(foodId))
                    },
                    onStoryClick = { storyId ->
                        navController.navigate(StoryDetailRoute(storyId))
                    }
                )
            }
            
            composable<PlaceDetailRoute> {
                val viewModel: PlaceDetailViewModel = hiltViewModel()
                val place by viewModel.place.collectAsStateWithLifecycle()
                
                place?.let { safePlace ->
                    PlaceDetailScreen(
                        place = safePlace,
                        onBackClick = { navController.popBackStack() }
                    )
                } ?: run {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
            
            composable<FeedRoute> { 
                FeedScreen(
                    onPlaceClick = { placeId ->
                        navController.navigate(PlaceDetailRoute(placeId))
                    }
                ) 
            }

            composable<CameraRoute> {
                CaptureScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable<StorylineRoute> {
                StorylineScreen()
            }

            composable<FoodDetailRoute> { backStackEntry ->
                val food = CultureContent.food(backStackEntry.toRoute<FoodDetailRoute>().foodId)
                if (food != null) {
                    FoodDetailScreen(
                        food = food,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }

            composable<StoryDetailRoute> { backStackEntry ->
                val story = CultureContent.story(backStackEntry.toRoute<StoryDetailRoute>().storyId)
                if (story != null) {
                    StoryDetailScreen(
                        story = story,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }



            composable<ProfileRoute> {
                ProfileScreen(
                    onNavigateToLogin = {
                        navController.navigate(AuthRoute) {
                            popUpTo(0)
                        }
                    }
                )
            }
        }
    }
}

/**
 * Giao diện màn hình giữ chỗ (Placeholder Screen) hiển thị cho các tính năng đang phát triển.
 *
 * @param title Tiêu đề hiển thị của màn hình đang phát triển.
 */
@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Đang phát triển: $title")
    }
}
