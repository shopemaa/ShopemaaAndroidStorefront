package com.shopemaa.android.storefront.ui.listeners

interface CartItemQuantityListener {
    fun onChange(productId: String, qty: Int)
}
