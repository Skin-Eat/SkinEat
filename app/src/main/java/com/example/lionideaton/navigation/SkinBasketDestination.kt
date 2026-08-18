package com.example.lionideaton.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.ui.graphics.vector.ImageVector

enum class SkinBasketDestination(
    val route: String,
    val label: String,
    val icon: ImageVector? = null
) {
    Home(route = "home", label = "홈", icon = Icons.Filled.Home),
    FoodLog(route = "foods", label = "음식기록", icon = Icons.Filled.RestaurantMenu),
    Analysis(route = "analysis", label = "분석"),
    Basket(route = "basket", label = "장바구니", icon = Icons.Filled.ShoppingBasket),
    Cart(route = "cart", label = "담은 재료"),
    Recipe(route = "recipe", label = "레시피 상세"),
    SkinLog(route = "skin", label = "피부기록"),
    Report(route = "report", label = "리포트", icon = Icons.Filled.BarChart),
    My(route = "my", label = "마이", icon = Icons.Filled.Person),
    SkinPhotoHistory(route = "skin_photo_history", label = "피부 사진 히스토리"),
    MealLogHistory(route = "meal_log_history", label = "식습관 로그 전체 보기");

    companion object {
        // Explicit order for the bottom bar: 홈 / 리포트 / (카메라 — a special center button
        // rendered separately, not a real destination) / 장바구니 / 마이. 음식기록 and 피부기록
        // are still real routes, just reached via the camera button's bottom sheet instead of a tab.
        val bottomBarOrder: List<SkinBasketDestination> = listOf(Home, Report, Basket, My)
    }
}
