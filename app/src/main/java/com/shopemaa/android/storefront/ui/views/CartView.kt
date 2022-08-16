package com.shopemaa.android.storefront.ui.views

import com.arellomobile.mvp.MvpView
import com.shopemaa.android.storefront.api.graphql.CartQuery
import com.shopemaa.android.storefront.errors.ApiError

interface CartView : MvpView {
    fun onCartSuccess(cart: CartQuery.Cart)
    fun onCartFailure(err: ApiError)
}
