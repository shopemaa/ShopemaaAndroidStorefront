package com.shopemaa.android.storefront.ui.presenters

import android.content.Context
import android.util.Log
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.google.gson.Gson
import com.shopemaa.android.storefront.api.ApiHelper
import com.shopemaa.android.storefront.api.graphql.OrderByCustomerEmailQuery
import com.shopemaa.android.storefront.contants.Constants
import com.shopemaa.android.storefront.errors.ApiError
import com.shopemaa.android.storefront.storage.CacheStorage
import com.shopemaa.android.storefront.ui.views.OrderView

@InjectViewState
class OrderPresenter : MvpPresenter<OrderView>() {

    suspend fun requestOrderDetails(ctx: Context, orderHash: String, customerEmail: String) {
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
                OrderByCustomerEmailQuery(orderHash, customerEmail)
            )
            .execute()
        if (resp.hasErrors()) {
            Log.d("error", Gson().toJson(resp.errors))
            viewState.onOrderDetailsFailure(ApiError())
            return
        }

        viewState.onOrderDetailsSuccess(resp.data!!.orderByCustomerEmail)
    }
}