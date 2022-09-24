package com.shopemaa.android.storefront.ui.presenters

import android.content.Context
import android.util.Log
import com.apollographql.apollo3.api.Optional
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.google.gson.Gson
import com.shopemaa.android.storefront.api.ApiHelper
import com.shopemaa.android.storefront.api.graphql.OrderGeneratePaymentNonceForGuestMutation
import com.shopemaa.android.storefront.api.graphql.type.PaymentRequestOverrides
import com.shopemaa.android.storefront.contants.Constants
import com.shopemaa.android.storefront.errors.ApiError
import com.shopemaa.android.storefront.storage.CacheStorage
import com.shopemaa.android.storefront.ui.views.DigitalPaymentView

@InjectViewState
class DigitalPaymentPresenter : MvpPresenter<DigitalPaymentView>() {

    suspend fun requestPaymentNonce(
        ctx: Context,
        orderHash: String,
        customerEmail: String,
        overrides: PaymentRequestOverrides?
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
            .mutation(
                OrderGeneratePaymentNonceForGuestMutation(
                    orderHash,
                    customerEmail,
                    Optional.presentIfNotNull(overrides)
                )
            )
            .execute()
        if (resp.hasErrors()) {
            Log.d("error", Gson().toJson(resp.errors))
            viewState.generatePaymentNonceFailure(ApiError())
            return
        }

        viewState.generatePaymentNonceSuccess(resp.data!!.orderGeneratePaymentNonceForGuest)
    }
}