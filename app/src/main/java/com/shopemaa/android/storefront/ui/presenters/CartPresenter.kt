package com.shopemaa.android.storefront.ui.presenters

import android.content.Context
import android.util.Log
import com.apollographql.apollo3.api.Optional
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.google.gson.Gson
import com.shopemaa.android.storefront.api.ApiHelper
import com.shopemaa.android.storefront.api.graphql.CartQuery
import com.shopemaa.android.storefront.api.graphql.NewCartMutation
import com.shopemaa.android.storefront.api.graphql.UpdateCartMutation
import com.shopemaa.android.storefront.api.graphql.type.CartItemAttributeParams
import com.shopemaa.android.storefront.api.graphql.type.CartItemParams
import com.shopemaa.android.storefront.api.graphql.type.NewCartParams
import com.shopemaa.android.storefront.api.graphql.type.UpdateCartParams
import com.shopemaa.android.storefront.contants.Constants
import com.shopemaa.android.storefront.errors.ApiError
import com.shopemaa.android.storefront.storage.CacheStorage
import com.shopemaa.android.storefront.ui.views.CartView
import com.shopemaa.android.storefront.utils.CartUtil

@InjectViewState
class CartPresenter : MvpPresenter<CartView>() {

    suspend fun requestCreateCart(
        ctx: Context,
        productId: String,
        attributes: MutableMap<String, String>
    ) {
        val c = CacheStorage(ctx)
        val storeKey = c.get(Constants.storeKeyLabel)
        val storeSecret = c.get(Constants.storeSecretLabel)

        val resp = ApiHelper
            .apolloClient(
                mutableMapOf(
                    "store-key" to storeKey,
                    "store-secret" to storeSecret
                )
            )
            .mutation(createNewCartQuery(productId, attributes))
            .execute()
        if (resp.hasErrors()) {
            viewState.onCartFailure(ApiError())
            return
        }

        viewState.onCartSuccess(CartUtil.newCartToCart(resp.data!!.newCart))
    }

    suspend fun requestUpdateCart(
        ctx: Context,
        cart: CartQuery.Cart,
        productId: String,
        attributes: MutableMap<String, String>
    ) {
        val c = CacheStorage(ctx)
        val storeKey = c.get(Constants.storeKeyLabel)
        val storeSecret = c.get(Constants.storeSecretLabel)

        val resp = ApiHelper
            .apolloClient(
                mutableMapOf(
                    "store-key" to storeKey,
                    "store-secret" to storeSecret
                )
            )
            .mutation(createUpdateCartQuery(cart, productId, attributes))
            .execute()
        if (resp.hasErrors()) {
            Log.d("Error", Gson().toJson(resp.errors).toString())
            viewState.onCartFailure(ApiError())
            return
        }

        viewState.onCartSuccess(CartUtil.updateCartToCart(resp.data!!.updateCart))
    }

    suspend fun requestUpdateCart(
        ctx: Context,
        cart: CartQuery.Cart,
        productId: String,
        qty: Int
    ) {
        val c = CacheStorage(ctx)
        val storeKey = c.get(Constants.storeKeyLabel)
        val storeSecret = c.get(Constants.storeSecretLabel)

        val resp = ApiHelper
            .apolloClient(
                mutableMapOf(
                    "store-key" to storeKey,
                    "store-secret" to storeSecret
                )
            )
            .mutation(createUpdateCartQuery(cart, productId, qty))
            .execute()
        if (resp.hasErrors()) {
            viewState.onCartFailure(ApiError())
            return
        }

        viewState.onCartSuccess(CartUtil.updateCartToCart(resp.data!!.updateCart))
    }

    private fun createNewCartQuery(
        productId: String,
        attributes: MutableMap<String, String>
    ): NewCartMutation {
        return NewCartMutation(
            NewCartParams(
                listOf(
                    CartItemParams(
                        productId,
                        if (attributes.isEmpty()) {
                            Optional.Absent
                        } else {
                            Optional.presentIfNotNull(
                                attributes.map {
                                    CartItemAttributeParams(it.key, it.value)
                                }.toList()
                            )
                        },
                        Optional.Absent,
                        1
                    )
                )
            )
        )
    }

    private fun createUpdateCartQuery(
        cart: CartQuery.Cart,
        productId: String,
        attributes: MutableMap<String, String>
    ): UpdateCartMutation {
        val items = mutableListOf<CartItemParams>()
        var isProductExists = false

        cart.cartItems.forEach { item ->
            var qty = 1
            if (item.product.id == productId) {
                qty = item.quantity + 1
                isProductExists = true
            }

            items.add(
                CartItemParams(
                    item.product.id,
                    Optional.presentIfNotNull(
                        item.attributes.map { at ->
                            val attr = item.product.attributes.find { it.name == at.name }
                            CartItemAttributeParams(attr?.id!!, at.selectedValue)
                        }
                    ),
                    Optional.Absent,
                    qty
                )
            )
        }

        if (!isProductExists) {
            items.add(
                CartItemParams(
                    productId,
                    Optional.presentIfNotNull(attributes.map {
                        CartItemAttributeParams(it.key, it.value)
                    }.toList()),
                    Optional.Absent,
                    1
                )
            )
        }

        Log.d(
            "Query", UpdateCartMutation(
                id = cart.id,
                params = UpdateCartParams(items)
            ).toString()
        )

        return UpdateCartMutation(
            id = cart.id,
            params = UpdateCartParams(items)
        )
    }

    private fun createUpdateCartQuery(
        cart: CartQuery.Cart,
        productId: String,
        qty: Int,
    ): UpdateCartMutation {
        val items = mutableListOf<CartItemParams>()
        cart.cartItems.forEach { item ->
            if (item.product.id == productId) {
                items.add(
                    CartItemParams(
                        item.product.id,
                        Optional.presentIfNotNull(
                            item.attributes.map { at ->
                                val attr = item.product.attributes.find { it.name == at.name }
                                CartItemAttributeParams(attr?.id!!, at.selectedValue)
                            }
                        ),
                        Optional.Absent,
                        qty
                    )
                )
            } else {
                items.add(
                    CartItemParams(
                        item.product.id,
                        Optional.presentIfNotNull(
                            item.attributes.map { at ->
                                val attr = item.product.attributes.find { it.name == at.name }
                                CartItemAttributeParams(attr?.id!!, at.selectedValue)
                            }
                        ),
                        Optional.Absent,
                        item.quantity
                    )
                )
            }
        }

        return UpdateCartMutation(
            id = cart.id,
            params = UpdateCartParams(items)
        )
    }
}
