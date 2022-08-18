package com.shopemaa.android.storefront.ui.presenters

import android.content.Context
import com.apollographql.apollo3.api.Optional
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.shopemaa.android.storefront.api.ApiHelper
import com.shopemaa.android.storefront.api.graphql.CategoriesQuery
import com.shopemaa.android.storefront.contants.Constants
import com.shopemaa.android.storefront.errors.ApiError
import com.shopemaa.android.storefront.storage.CacheStorage
import com.shopemaa.android.storefront.ui.views.CategoryView

@InjectViewState
class CategoryPresenter : MvpPresenter<CategoryView>() {

    suspend fun requestCategories(ctx: Context, query: String?, page: Int, limit: Int) {
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
            .query(
                CategoriesQuery(
                    query = Optional.presentIfNotNull(query),
                    page = page,
                    limit = limit
                )
            )
            .execute()
        if (resp.hasErrors()) {
            viewState.onCategoriesFailure(ApiError())
            return
        }

        viewState.onCategoriesSuccess(resp.data!!.categories)
    }
}
