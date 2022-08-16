package com.shopemaa.android.storefront.ui.presenters

import android.content.Context
import com.apollographql.apollo3.api.Optional
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.shopemaa.android.storefront.api.ApiHelper
import com.shopemaa.android.storefront.api.graphql.CartQuery
import com.shopemaa.android.storefront.api.graphql.NewCartMutation
import com.shopemaa.android.storefront.api.graphql.UpdateCartMutation
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

    suspend fun requestCreateCart(ctx: Context, productId: String) {
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
            .mutation(createNewCartQuery(productId))
            .execute()
        if (resp.hasErrors()) {
            viewState.onCartFailure(ApiError())
            return
        }

        viewState.onCartSuccess(CartUtil.newCartToCart(resp.data!!.newCart))
    }

    suspend fun requestUpdateCart(ctx: Context, cart: CartQuery.Cart, productId: String) {
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
            .mutation(createUpdateCartQuery(cart, productId))
            .execute()
        if (resp.hasErrors()) {
            viewState.onCartFailure(ApiError())
            return
        }

        viewState.onCartSuccess(CartUtil.updateCartToCart(resp.data!!.updateCart))
    }

    suspend fun requestUpdateCart(ctx: Context, cart: CartQuery.Cart, productId: String, qty: Int) {
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

    private fun createNewCartQuery(productId: String): NewCartMutation {
        return NewCartMutation(
            NewCartParams(
                listOf(
                    CartItemParams(
                        productId,
                        Optional.Absent,
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
    ): UpdateCartMutation {
        val items = mutableListOf<CartItemParams>()
        var isProductExists = false

        cart.cartItems.forEach {
            var qty = 1
            if (it.product.id == productId) {
                qty = it.quantity + 1
                isProductExists = true
            }

            items.add(
                CartItemParams(
                    it.product.id,
                    Optional.Absent,
                    Optional.Absent,
                    qty
                )
            )
        }

        if (!isProductExists) {
            items.add(
                CartItemParams(
                    productId,
                    Optional.Absent,
                    Optional.Absent,
                    1
                )
            )
        }

        return UpdateCartMutation(
            id = cart.id,
            params = UpdateCartParams(items)
        )
    }

    private fun createUpdateCartQuery(
        cart: CartQuery.Cart,
        productId: String,
        qty: Int
    ): UpdateCartMutation {
        val items = mutableListOf<CartItemParams>()
        cart.cartItems.forEach {
            if (it.product.id == productId) {
                items.add(
                    CartItemParams(
                        it.product.id,
                        Optional.Absent,
                        Optional.Absent,
                        qty
                    )
                )
            } else {
                items.add(
                    CartItemParams(
                        it.product.id,
                        Optional.Absent,
                        Optional.Absent,
                        it.quantity
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
