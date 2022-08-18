package com.shopemaa.android.storefront.ui.presenters

import android.content.Context
import com.apollographql.apollo3.api.Optional
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.shopemaa.android.storefront.api.ApiHelper
import com.shopemaa.android.storefront.api.graphql.CategoriesQuery
import com.shopemaa.android.storefront.api.graphql.ProductsQuery
import com.shopemaa.android.storefront.api.graphql.type.FilterKey
import com.shopemaa.android.storefront.api.graphql.type.FilterQuery
import com.shopemaa.android.storefront.api.graphql.type.Search
import com.shopemaa.android.storefront.contants.Constants
import com.shopemaa.android.storefront.errors.ApiError
import com.shopemaa.android.storefront.storage.CacheStorage
import com.shopemaa.android.storefront.ui.views.ProductListView

@InjectViewState
class ProductListPresenter : MvpPresenter<ProductListView>() {

    suspend fun requestProducts(
        ctx: Context,
        page: Int,
        limit: Int,
        filters: MutableList<FilterQuery>
    ) {
        val c = CacheStorage(ctx)
        val storeKey = c.get(Constants.storeKeyLabel)
        val storeSecret = c.get(Constants.storeSecretLabel)

        val search = Search(Optional.Absent, filters)

        val resp = ApiHelper
            .apolloClient(
                mutableMapOf(
                    "store-key" to storeKey,
                    "store-secret" to storeSecret
                )
            )
            .query(ProductsQuery(search, page, limit))
            .execute()
        if (resp.hasErrors()) {
            viewState.onProductsError(ApiError())
            return
        }

        viewState.onProducts(resp.data!!.productSearch)
    }

    suspend fun requestProducts(
        ctx: Context,
        query: String,
        page: Int,
        limit: Int,
        filters: MutableList<FilterQuery>
    ) {
        val c = CacheStorage(ctx)
        val storeKey = c.get(Constants.storeKeyLabel)
        val storeSecret = c.get(Constants.storeSecretLabel)

        val search = Search(Optional.presentIfNotNull(query), filters)

        val resp = ApiHelper
            .apolloClient(
                mutableMapOf(
                    "store-key" to storeKey,
                    "store-secret" to storeSecret
                )
            )
            .query(ProductsQuery(search, page, limit))
            .execute()
        if (resp.hasErrors()) {
            viewState.onProductsError(ApiError())
            return
        }

        viewState.onProducts(resp.data!!.productSearch)
    }
}
