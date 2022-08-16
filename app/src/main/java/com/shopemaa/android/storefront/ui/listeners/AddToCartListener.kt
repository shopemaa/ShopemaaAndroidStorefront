package com.shopemaa.android.storefront.ui.listeners

interface AddToCartListener {
    fun onAdd(productId: String)
    fun onOutOfStock(msg: String)
}
