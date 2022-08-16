package com.shopemaa.android.storefront.ui.views

import com.arellomobile.mvp.MvpView
import com.shopemaa.android.storefront.api.graphql.ProductsQuery
import com.shopemaa.android.storefront.errors.ApiError

interface ProductListView : MvpView {
    fun onProducts(products: List<ProductsQuery.ProductSearch>)
    fun onProductsError(err: ApiError)
}
