package com.shopemaa.android.storefront.utils

import com.google.gson.Gson
import com.shopemaa.android.storefront.api.graphql.CartQuery
import com.shopemaa.android.storefront.api.graphql.NewCartMutation
import com.shopemaa.android.storefront.api.graphql.UpdateCartMutation
import com.shopemaa.android.storefront.contants.Constants
import com.shopemaa.android.storefront.storage.ICacheStorage

object CartUtil {
    fun newCartToCart(newCart: NewCartMutation.NewCart): CartQuery.Cart {
        val cartItems = mutableListOf<CartQuery.CartItem>()
        newCart.cartItems.forEach {
            cartItems.add(
                CartQuery.CartItem(
                    id = it.id,
                    quantity = it.quantity,
                    purchasePrice = it.purchasePrice,
                    product = CartQuery.Product(
                        id = it.product.id,
                        name = it.product.name,
                        slug = it.product.slug,
                        description = it.product.description,
                        sku = it.product.sku,
                        price = it.product.price,
                        stock = it.product.stock,
                        images = it.product.images,
                        fullImages = it.product.fullImages,
                        isDigitalProduct = it.product.isDigitalProduct,
                        views = it.product.views,
                        productUnit = it.product.productUnit,
                        createdAt = it.product.createdAt,
                        updatedAt = it.product.updatedAt,
                        productSpecificDiscount = it.product.productSpecificDiscount,
                        attributes = it.product.attributes.map { at ->
                            CartQuery.Attribute(at.id, at.name, at.values, at.isRequired)
                        }
                    ),
                    attributes = it.attributes.map { a ->
                        CartQuery.Attribute1(
                            a.name,
                            a.selectedValue
                        )
                    },
                    variation = if (it.variation == null) {
                        null
                    } else {
                        CartQuery.Variation(
                            id = it.variation.id,
                            name = it.variation.name,
                            price = it.variation.price,
                            sku = it.variation.sku,
                            stock = it.variation.stock
                        )
                    }
                )
            )
        }
        return CartQuery.Cart(newCart.id, newCart.isShippingRequired, cartItems)
    }

    fun updateCartToCart(newCart: UpdateCartMutation.UpdateCart): CartQuery.Cart {
        val cartItems = mutableListOf<CartQuery.CartItem>()
        newCart.cartItems.forEach {
            cartItems.add(
                CartQuery.CartItem(
                    id = it.id,
                    quantity = it.quantity,
                    purchasePrice = it.purchasePrice,
                    product = CartQuery.Product(
                        id = it.product.id,
                        name = it.product.name,
                        slug = it.product.slug,
                        description = it.product.description,
                        sku = it.product.sku,
                        price = it.product.price,
                        stock = it.product.stock,
                        images = it.product.images,
                        fullImages = it.product.fullImages,
                        isDigitalProduct = it.product.isDigitalProduct,
                        views = it.product.views,
                        productUnit = it.product.productUnit,
                        createdAt = it.product.createdAt,
                        updatedAt = it.product.updatedAt,
                        productSpecificDiscount = it.product.productSpecificDiscount,
                        attributes = it.product.attributes.map { at ->
                            CartQuery.Attribute(at.id, at.name, at.values, at.isRequired)
                        }
                    ),
                    attributes = it.attributes.map { a ->
                        CartQuery.Attribute1(
                            a.name,
                            a.selectedValue
                        )
                    },
                    variation = if (it.variation == null) {
                        null
                    } else {
                        CartQuery.Variation(
                            id = it.variation.id,
                            name = it.variation.name,
                            price = it.variation.price,
                            sku = it.variation.sku,
                            stock = it.variation.stock
                        )
                    }
                )
            )
        }
        return CartQuery.Cart(newCart.id, newCart.isShippingRequired, cartItems)
    }

    fun cartFromCache(c: ICacheStorage): CartQuery.Cart {
        return Gson().fromJson(
            c.get(Constants.cartLabel),
            CartQuery.Cart::class.java
        )
    }

    fun cartToCache(c: ICacheStorage, cart: CartQuery.Cart) {
        val j = Gson().toJson(cart)
        c.save(Constants.cartLabel, j)
    }
}
