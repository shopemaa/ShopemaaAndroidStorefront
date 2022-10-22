package com.shopemaa.android.storefront.ui.presenters

import android.content.Context
import android.util.Log
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.shopemaa.android.storefront.api.ApiHelper
import com.shopemaa.android.storefront.api.graphql.StoreBySecretQuery
import com.shopemaa.android.storefront.contants.Constants
import com.shopemaa.android.storefront.errors.ApiError
import com.shopemaa.android.storefront.storage.CacheStorage
import com.shopemaa.android.storefront.ui.views.StoreView

@InjectViewState
class StorePresenter : MvpPresenter<StoreView>() {

    suspend fun requestStore(ctx: Context) {
        val c = CacheStorage(ctx)
        val storeKey = c.get(Constants.storeKeyLabel)
        val storeSecret = c.get(Constants.storeSecretLabel)

        try {
            val resp = ApiHelper
                .apolloClient(
                    mutableMapOf(
                        "store-key" to storeKey,
                        "store-secret" to storeSecret
                    )
                )
                .query(StoreBySecretQuery())
                .execute()
            if (resp.hasErrors()) {
                viewState.onStoreFailure(ApiError())
                return
            }

            viewState.onStoreSuccess(resp.data!!.storeBySecret)
        } catch (e: Exception) {
            Log.d("Exception", e.message!!)
            if ("Failed to execute GraphQL http network request" == e.message) {
                viewState.internetUnavailable()
            }
        }
    }
}
