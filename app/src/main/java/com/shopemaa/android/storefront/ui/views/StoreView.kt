package com.shopemaa.android.storefront.ui.views

import com.arellomobile.mvp.MvpView
import com.shopemaa.android.storefront.api.graphql.StoreBySecretQuery
import com.shopemaa.android.storefront.errors.ApiError

interface StoreView : MvpView {
    fun onStoreSuccess(store: StoreBySecretQuery.StoreBySecret)
    fun onStoreFailure(err: ApiError)
    fun internetUnavailable()
}
