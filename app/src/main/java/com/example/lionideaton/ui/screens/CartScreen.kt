package com.example.lionideaton.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lionideaton.ui.theme.CardWhite
import com.example.lionideaton.ui.theme.CoralPrimary
import com.example.lionideaton.ui.theme.SurfaceMuted
import com.example.lionideaton.ui.theme.TextPrimary
import com.example.lionideaton.ui.theme.TextSecondary
import java.net.URLEncoder

private data class SuggestedMealPlan(val day: String, val menu: String)

private val suggestedMealPlans = listOf(
    SuggestedMealPlan("1일차 식단", "브로콜리 연어 샐러드"),
    SuggestedMealPlan("2일차 식단", "미니 토마토 오믈렛")
)

@Composable
fun CartScreen(modifier: Modifier = Modifier, cartViewModel: CartViewModel, onRecipeClick: () -> Unit = {}) {
    val cartItems by cartViewModel.items.collectAsState()
    val total = cartItems.sumOf { it.unitPrice * it.quantity }
    val context = LocalContext.current

    if (cartItems.isEmpty()) {
        EmptyCartMessage(modifier = modifier)
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            cartItems.forEach { item ->
                CartItemRow(
                    item = item,
                    onIncrease = { cartViewModel.increaseQuantity(item.name) },
                    onDecrease = { cartViewModel.decreaseQuantity(item.name) }
                )
            }
        }

        MealPlanPreviewCard(plans = suggestedMealPlans, onRecipeClick = onRecipeClick)

        ShoppingIntegrationSection(
            items = cartItems,
            onSearch = { store, query ->
                val url = shoppingSearchUrl(store, query)
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        )

        Button(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary, contentColor = Color.White)
        ) {
            Text(text = "총 ${"%,d".format(total)}원 주문하기", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EmptyCartMessage(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "아직 담은 재료가 없어요.\n추천식재료 화면에서 담아보세요!",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

@Composable
private fun CartItemRow(item: CartItem, onIncrease: () -> Unit, onDecrease: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(10.dp),
                color = SurfaceMuted
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = item.icon, contentDescription = null, tint = CoralPrimary, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${"%,d".format(item.unitPrice)}원",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CoralPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Surface(shape = RoundedCornerShape(50), color = SurfaceMuted) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
                    IconButton(onClick = onDecrease) {
                        Icon(imageVector = Icons.Filled.Remove, contentDescription = "수량 줄이기")
                    }
                    Text(
                        text = "${item.quantity}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    IconButton(onClick = onIncrease) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = "수량 늘리기")
                    }
                }
            }
        }
    }
}

@Composable
private fun MealPlanPreviewCard(plans: List<SuggestedMealPlan>, onRecipeClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "배송된 재료로 만들 요리",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            plans.forEach { plan ->
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(50), color = SurfaceMuted) {
                        Text(
                            text = plan.day,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = plan.menu, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            Divider(color = SurfaceMuted)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.clickable(onClick = onRecipeClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "조리법 미리 확인하기",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CoralPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Filled.OpenInNew,
                    contentDescription = null,
                    tint = CoralPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// 여러 재료를 한 번에 합쳐서 검색하면(예: "연어 브로콜리 토마토") 쇼핑몰 검색 결과가
// 뒤죽박죽 나와서 쓸모가 없어, 재료별로 따로 검색 링크를 연다 — 브라우즈 전용 딥링크(API
// 연동/장바구니 반영 없음). 실제 결제는 스펙 범위 밖.
private enum class ShoppingStore(val label: String) {
    COUPANG("쿠팡"),
    NAVER("네이버 스토어")
}

private fun shoppingSearchUrl(store: ShoppingStore, query: String): String {
    val encoded = URLEncoder.encode(query, "UTF-8")
    return when (store) {
        ShoppingStore.COUPANG -> "https://www.coupang.com/np/search?q=$encoded"
        ShoppingStore.NAVER -> "https://search.shopping.naver.com/search/all?query=$encoded"
    }
}

@Composable
private fun ShoppingIntegrationSection(items: List<CartItem>, onSearch: (ShoppingStore, String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "재료별로 구매처 찾기",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        items.forEach { item ->
            IngredientShoppingCard(
                name = item.name,
                onCoupangClick = { onSearch(ShoppingStore.COUPANG, item.name) },
                onNaverClick = { onSearch(ShoppingStore.NAVER, item.name) }
            )
        }
    }
}

@Composable
private fun IngredientShoppingCard(name: String, onCoupangClick: () -> Unit, onNaverClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SurfaceMuted
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            ShoppingStoreRow(label = "쿠팡에서 검색", onClick = onCoupangClick)
            Spacer(modifier = Modifier.height(6.dp))
            ShoppingStoreRow(label = "네이버 스토어에서 검색", onClick = onNaverClick)
        }
    }
}

@Composable
private fun ShoppingStoreRow(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = Icons.Filled.Storefront, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            modifier = Modifier.weight(1f)
        )
        Icon(imageVector = Icons.Filled.OpenInNew, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
    }
}
