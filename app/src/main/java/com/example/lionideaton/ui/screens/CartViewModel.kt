package com.example.lionideaton.ui.screens

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CartItem(
    val name: String,
    val unitPrice: Int,
    val quantity: Int,
    val icon: ImageVector
)

// Scoped to the Activity (see MainActivity) so the Basket screen (add items) and
// Cart screen (review/checkout) share the same in-memory cart.
class CartViewModel : ViewModel() {

    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items: StateFlow<List<CartItem>> = _items.asStateFlow()

    fun addItem(name: String, unitPrice: Int, icon: ImageVector) {
        _items.update { current ->
            if (current.any { it.name == name }) current
            else current + CartItem(name, unitPrice, quantity = 1, icon = icon)
        }
    }

    fun increaseQuantity(name: String) {
        _items.update { list -> list.map { if (it.name == name) it.copy(quantity = it.quantity + 1) else it } }
    }

    fun decreaseQuantity(name: String) {
        _items.update { list ->
            list.mapNotNull { item ->
                when {
                    item.name != name -> item
                    item.quantity <= 1 -> null
                    else -> item.copy(quantity = item.quantity - 1)
                }
            }
        }
    }
}
