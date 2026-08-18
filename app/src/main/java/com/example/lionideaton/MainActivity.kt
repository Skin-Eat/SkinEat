
package com.example.lionideaton

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.lionideaton.navigation.SkinBasketDestination
import com.example.lionideaton.ui.screens.AnalysisScreen
import com.example.lionideaton.ui.screens.AuthScreen
import com.example.lionideaton.ui.screens.BasketScreen
import com.example.lionideaton.ui.screens.CartScreen
import com.example.lionideaton.ui.screens.CartViewModel
import com.example.lionideaton.ui.screens.FoodLogScreen
import com.example.lionideaton.ui.screens.HomeScreen
import com.example.lionideaton.ui.screens.MealLogHistoryScreen
import com.example.lionideaton.ui.screens.MealLogViewModel
import com.example.lionideaton.ui.screens.MyScreen
import com.example.lionideaton.ui.screens.OnboardingScreen
import com.example.lionideaton.ui.screens.RecipeScreen
import com.example.lionideaton.ui.screens.ReportScreen
import com.example.lionideaton.ui.screens.SkinLogScreen
import com.example.lionideaton.ui.screens.SkinLogViewModel
import com.example.lionideaton.ui.screens.SkinPhotoHistoryScreen
import com.example.lionideaton.ui.screens.UserProfileViewModel
import com.example.lionideaton.ui.theme.CoralPrimary
import com.example.lionideaton.ui.theme.LionideatonTheme
import com.example.lionideaton.ui.theme.PeachSecondary
import com.example.lionideaton.ui.theme.SurfaceMuted
import com.example.lionideaton.ui.theme.TextPrimary
import com.example.lionideaton.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LionideatonTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    SkinBasketApp()
                }
            }
        }
    }
}

// Screens whose mockup shows a back button + its own title instead of the generic
// "Skin Basket" bar with the bell icon. Most of these are detail screens pushed on top of a
// bottom-nav tab, but Report is a bottom-nav tab itself whose design still includes a back arrow
// (the bottom bar still renders separately/independently of this, so it stays highlighted).
private val detailScreenTitles = mapOf(
    SkinBasketDestination.Analysis.route to "식단 피부 분석",
    SkinBasketDestination.SkinLog.route to "피부기록",
    SkinBasketDestination.Cart.route to "담은 재료",
    SkinBasketDestination.Recipe.route to "레시피 상세",
    SkinBasketDestination.Report.route to "피부 분석 리포트",
    SkinBasketDestination.SkinPhotoHistory.route to "피부 사진 히스토리",
    SkinBasketDestination.MealLogHistory.route to "식습관 로그 전체 보기"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkinBasketApp() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val detailTitle = detailScreenTitles[currentRoute]
    // Scoped to this composable's owner (the Activity), so Basket and Cart screens share one cart.
    val cartViewModel: CartViewModel = viewModel()
    val cartItems by cartViewModel.items.collectAsState()
    // Same idea: shared across Home/FoodLog/Analysis/Basket so a logged meal shows up everywhere.
    val mealLogViewModel: MealLogViewModel = viewModel()
    // Shared across SkinLog/Report/My so a captured skin photo shows up in the Before/After + history.
    val skinLogViewModel: SkinLogViewModel = viewModel()
    // Shared across Auth/Onboarding/Home/My so sign-in state + the survey result show up everywhere.
    val userProfileViewModel: UserProfileViewModel = viewModel()
    val isLoggedIn by userProfileViewModel.isLoggedIn.collectAsState()
    val onboardingCompleted by userProfileViewModel.onboardingCompleted.collectAsState()
    val isHomeRoute = currentRoute == SkinBasketDestination.Home.route
    val showAuth = isHomeRoute && !isLoggedIn
    val showOnboarding = isHomeRoute && isLoggedIn && !onboardingCompleted
    val hideChrome = showAuth || showOnboarding
    var showCameraSheet by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            when {
                hideChrome -> Unit

                // Home renders its own calendar/bell/profile header, so no generic bar here.
                isHomeRoute -> Unit

                detailTitle != null -> CenterAlignedTopAppBar(
                    title = { Text(text = detailTitle) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "뒤로가기")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )

                currentRoute == SkinBasketDestination.Basket.route -> CenterAlignedTopAppBar(
                    title = { Text(text = "장바구니") },
                    actions = {
                        IconButton(onClick = { navController.navigate(SkinBasketDestination.Cart.route) }) {
                            BadgedBox(badge = {
                                if (cartItems.isNotEmpty()) Badge { Text(text = "${cartItems.size}") }
                            }) {
                                Icon(imageVector = Icons.Filled.ShoppingCart, contentDescription = "장바구니")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )

                currentRoute == SkinBasketDestination.My.route -> CenterAlignedTopAppBar(
                    title = { Text(text = "마이페이지") },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(imageVector = Icons.Filled.Settings, contentDescription = "설정")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )

                else -> CenterAlignedTopAppBar(
                    title = { Text(text = "Skin Basket") },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(imageVector = Icons.Filled.Notifications, contentDescription = "알림")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        bottomBar = {
            if (!hideChrome) {
                SkinBasketBottomBar(navController, onCameraClick = { showCameraSheet = true })
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = SkinBasketDestination.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(SkinBasketDestination.Home.route) {
                when {
                    !isLoggedIn -> AuthScreen(userProfileViewModel = userProfileViewModel)
                    !onboardingCompleted -> OnboardingScreen(userProfileViewModel = userProfileViewModel)
                    else -> HomeScreen(
                        onLogFoodClick = { navController.navigate(SkinBasketDestination.FoodLog.route) },
                        onSkinScoreClick = { navController.navigate(SkinBasketDestination.Analysis.route) },
                        onProfileClick = {
                            navController.navigate(SkinBasketDestination.My.route) {
                                popUpTo(SkinBasketDestination.Home.route)
                                launchSingleTop = true
                            }
                        },
                        mealLogViewModel = mealLogViewModel,
                        userProfileViewModel = userProfileViewModel
                    )
                }
            }
            composable(SkinBasketDestination.FoodLog.route) { FoodLogScreen(mealLogViewModel = mealLogViewModel) }
            composable(SkinBasketDestination.Analysis.route) { AnalysisScreen(mealLogViewModel = mealLogViewModel) }
            composable(SkinBasketDestination.Basket.route) {
                BasketScreen(
                    cartViewModel = cartViewModel,
                    mealLogViewModel = mealLogViewModel,
                    userProfileViewModel = userProfileViewModel,
                    onRecipeClick = { navController.navigate(SkinBasketDestination.Recipe.route) }
                )
            }
            composable(SkinBasketDestination.Cart.route) {
                CartScreen(cartViewModel = cartViewModel)
            }
            composable(SkinBasketDestination.Recipe.route) { RecipeScreen() }
            composable(SkinBasketDestination.SkinLog.route) { SkinLogScreen(skinLogViewModel = skinLogViewModel) }
            composable(SkinBasketDestination.Report.route) {
                ReportScreen(skinLogViewModel = skinLogViewModel, mealLogViewModel = mealLogViewModel)
            }
            composable(SkinBasketDestination.My.route) {
                MyScreen(
                    cartViewModel = cartViewModel,
                    skinLogViewModel = skinLogViewModel,
                    userProfileViewModel = userProfileViewModel,
                    mealLogViewModel = mealLogViewModel,
                    onSkinPhotoHistoryClick = { navController.navigate(SkinBasketDestination.SkinPhotoHistory.route) },
                    onMealLogHistoryClick = { navController.navigate(SkinBasketDestination.MealLogHistory.route) }
                )
            }
            composable(SkinBasketDestination.SkinPhotoHistory.route) {
                SkinPhotoHistoryScreen(skinLogViewModel = skinLogViewModel)
            }
            composable(SkinBasketDestination.MealLogHistory.route) {
                MealLogHistoryScreen(mealLogViewModel = mealLogViewModel)
            }
        }
    }

    if (showCameraSheet) {
        ModalBottomSheet(onDismissRequest = { showCameraSheet = false }) {
            CameraChoiceSheet(
                onFoodLogClick = {
                    showCameraSheet = false
                    navController.navigate(SkinBasketDestination.FoodLog.route)
                },
                onSkinLogClick = {
                    showCameraSheet = false
                    navController.navigate(SkinBasketDestination.SkinLog.route)
                }
            )
        }
    }
}

// Left side (홈/리포트) + a reserved empty center slot (the camera FAB floats above it) +
// right side (장바구니/마이) — this keeps the 4 real tabs visually balanced around the center button.
@Composable
private fun SkinBasketBottomBar(navController: NavHostController, onCameraClick: () -> Unit) {
    val currentDestination by navController.currentBackStackEntryAsState()
    val currentRoute = currentDestination?.destination?.route
    val leftItems = listOf(SkinBasketDestination.Home, SkinBasketDestination.Report)
    val rightItems = listOf(SkinBasketDestination.Basket, SkinBasketDestination.My)

    Box {
        NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
            leftItems.forEach { destination ->
                BottomBarItem(destination = destination, selected = currentRoute == destination.route, navController = navController)
            }
            NavigationBarItem(
                selected = false,
                enabled = false,
                onClick = {},
                icon = {},
                colors = NavigationBarItemDefaults.colors(
                    disabledIconColor = Color.Transparent,
                    disabledTextColor = Color.Transparent
                )
            )
            rightItems.forEach { destination ->
                BottomBarItem(destination = destination, selected = currentRoute == destination.route, navController = navController)
            }
        }
        FloatingActionButton(
            onClick = onCameraClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-22).dp)
                .size(56.dp),
            shape = CircleShape,
            containerColor = CoralPrimary,
            contentColor = Color.White
        ) {
            Icon(imageVector = Icons.Filled.PhotoCamera, contentDescription = "카메라")
        }
    }
}

@Composable
private fun RowScope.BottomBarItem(destination: SkinBasketDestination, selected: Boolean, navController: NavHostController) {
    NavigationBarItem(
        selected = selected,
        onClick = {
            if (!selected) {
                navController.navigate(destination.route) {
                    popUpTo(SkinBasketDestination.Home.route)
                    launchSingleTop = true
                }
            }
        },
        icon = {
            destination.icon?.let {
                Icon(imageVector = it, contentDescription = destination.label)
            }
        },
        label = { Text(text = destination.label) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = CoralPrimary,
            selectedTextColor = CoralPrimary,
            indicatorColor = PeachSecondary,
            unselectedIconColor = TextSecondary,
            unselectedTextColor = TextSecondary
        )
    )
}

@Composable
private fun CameraChoiceSheet(onFoodLogClick: () -> Unit, onSkinLogClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "무엇을 기록할까요?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(16.dp))
        CameraChoiceRow(icon = Icons.Filled.Restaurant, label = "음식 기록하기", onClick = onFoodLogClick)
        Spacer(modifier = Modifier.height(10.dp))
        CameraChoiceRow(icon = Icons.Filled.Face, label = "피부 사진 기록하기", onClick = onSkinLogClick)
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun CameraChoiceRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceMuted)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = CoralPrimary)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary)
        }
    }
}
