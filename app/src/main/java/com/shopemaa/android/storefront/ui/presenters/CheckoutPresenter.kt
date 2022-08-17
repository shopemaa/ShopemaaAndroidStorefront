package com.shopemaa.android.storefront.ui.presenters

import android.content.Context
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.shopemaa.android.storefront.api.ApiHelper
import com.shopemaa.android.storefront.api.graphql.CountriesQuery
import com.shopemaa.android.storefront.api.graphql.PaymentMethodsQuery
import com.shopemaa.android.storefront.api.graphql.ShippingMethodsQuery
import com.shopemaa.android.storefront.contants.Constants
import com.shopemaa.android.storefront.errors.ApiError
import com.shopemaa.android.storefront.storage.CacheStorage
import com.shopemaa.android.storefront.ui.views.CheckoutView

@InjectViewState
class CheckoutPresenter : MvpPresenter<CheckoutView>() {

    suspend fun requestCountries(ctx: Context) {
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
            .query(CountriesQuery())
            .execute()
        if (resp.hasErrors()) {
            viewState.onCountryListFailure(ApiError())
            return
        }

        viewState.onCountryListSuccess(resp.data!!.locations)
    }

    suspend fun requestPaymentMethods(ctx: Context) {
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
            .query(PaymentMethodsQuery())
            .execute()
        if (resp.hasErrors()) {
            viewState.onPaymentMethodListFailure(ApiError())
            return
        }

        viewState.onPaymentMethodListSuccess(resp.data!!.paymentMethods)
    }

    suspend fun requestShippingMethods(ctx: Context) {
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
            .query(ShippingMethodsQuery())
            .execute()
        if (resp.hasErrors()) {
            viewState.onShippingMethodListFailure(ApiError())
            return
        }

        viewState.onShippingMethodListSuccess(resp.data!!.shippingMethods)
    }
}
