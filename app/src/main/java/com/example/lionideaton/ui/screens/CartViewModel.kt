package com.example.lionideaton.ui.screens

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lionideaton.data.IngredientSeedData
import com.example.lionideaton.data.model.Ingredient
import com.example.lionideaton.data.model.PriceBand
import com.example.lionideaton.data.network.BasketItemIn
import com.example.lionideaton.data.network.BasketItemOut
import com.example.lionideaton.data.network.BasketItemUpdate
import com.example.lionideaton.data.network.NetworkModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CartItem(
    val backendId: Int? = null,
    val name: String,
    val unitPrice: Int,
    val quantity: Int,
    val icon: ImageVector
)

// Scoped to the Activity (see MainActivity) so the Basket screen (add items) and
// Cart screen (review/checkout) share the same in-memory cart. Mirrors the "optimistic
// local update, best-effort backend sync" pattern used by MealLogViewModel — a failed
// network call never breaks the local UX.
class CartViewModel : ViewModel() {

    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items: StateFlow<List<CartItem>> = _items.asStateFlow()

    fun addItem(ingredient: Ingredient, keyNutrient: String) {
        val name = ingredient.name
        if (_items.value.any { it.name == name }) return
        _items.update { current ->
            current + CartItem(
                name = name,
                unitPrice = IngredientSeedData.estimatedPrice(ingredient.priceBand),
                quantity = 1,
                icon = IngredientSeedData.iconFor(keyNutrient)
            )
        }

        if (NetworkModule.authToken == null) return
        viewModelScope.launch {
            try {
                // 안드로이드 로컬 시드의 id는 백엔드 ingredient 테이블 id와 안 맞을 수 있어
                // 이름+key_nutrient로 실제 id를 다시 찾는다(resolveBackendFoodId와 동일한 이유).
                val backendIngredientId = NetworkModule.api
                    .searchIngredients(keyNutrient = keyNutrient, name = name)
                    .data?.firstOrNull()?.id ?: return@launch
                val created = NetworkModule.api
                    .addBasketItem(BasketItemIn(ingredientId = backendIngredientId))
                    .data ?: return@launch
                _items.update { list ->
                    list.map { if (it.name == name) it.copy(backendId = created.id, quantity = created.quantity) else it }
                }
            } catch (e: Exception) {
                // 로컬 카트는 이미 갱신됐으니 화면은 정상 동작 — 백엔드 반영만 실패한 것.
                // TODO: 재시도/오프라인 큐 없음. 데모 이후 개선 대상.
            }
        }
    }

    fun increaseQuantity(name: String) {
        _items.update { list -> list.map { if (it.name == name) it.copy(quantity = it.quantity + 1) else it } }
        syncQuantity(name)
    }

    fun decreaseQuantity(name: String) {
        val willBeRemoved = _items.value.firstOrNull { it.name == name && it.quantity <= 1 }
        _items.update { list ->
            list.mapNotNull { item ->
                when {
                    item.name != name -> item
                    item.quantity <= 1 -> null
                    else -> item.copy(quantity = item.quantity - 1)
                }
            }
        }
        val backendId = willBeRemoved?.backendId
        if (willBeRemoved != null) {
            if (backendId != null) deleteRemote(backendId)
        } else {
            syncQuantity(name)
        }
    }

    private fun syncQuantity(name: String) {
        val item = _items.value.firstOrNull { it.name == name } ?: return
        val backendId = item.backendId ?: return
        viewModelScope.launch {
            try {
                NetworkModule.api.updateBasketItem(backendId, BasketItemUpdate(item.quantity))
            } catch (e: Exception) {
                // TODO: 재시도/오프라인 큐 없음.
            }
        }
    }

    private fun deleteRemote(backendId: Int) {
        viewModelScope.launch {
            try {
                NetworkModule.api.deleteBasketItem(backendId)
            } catch (e: Exception) {
                // TODO: 재시도/오프라인 큐 없음.
            }
        }
    }

    // 로그인 직후(또는 앱 재시작 후 세션 복원 시) 백엔드에 저장된 장바구니를 불러온다 —
    // MealLogViewModel.loadFromBackend()와 같은 이유: authToken이 메모리에만 있던 시절엔
    // 앱을 껐다 켜면 로컬 카트가 그냥 비어 있었다.
    fun loadFromBackend() {
        if (NetworkModule.authToken == null) return
        viewModelScope.launch {
            try {
                _items.value = NetworkModule.api.getBasket().data.orEmpty().map { it.toCartItem() }
            } catch (e: Exception) {
                // 조회 실패 시 기존 로컬 상태 유지.
            }
        }
    }

    private fun BasketItemOut.toCartItem(): CartItem {
        val priceBand = ingredient.priceBand?.let { raw -> runCatching { PriceBand.valueOf(raw) }.getOrNull() }
        return CartItem(
            backendId = id,
            name = ingredient.name,
            unitPrice = IngredientSeedData.estimatedPrice(priceBand),
            quantity = quantity,
            icon = IngredientSeedData.iconFor(ingredient.keyNutrient)
        )
    }
}
