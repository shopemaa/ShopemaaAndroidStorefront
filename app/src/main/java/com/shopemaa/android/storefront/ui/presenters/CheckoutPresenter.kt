package com.shopemaa.android.storefront.ui.presenters

import android.content.Context
import android.util.Log
import com.apollographql.apollo3.api.Optional
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.google.gson.Gson
import com.shopemaa.android.storefront.api.ApiHelper
import com.shopemaa.android.storefront.api.graphql.*
import com.shopemaa.android.storefront.api.graphql.type.GuestCheckoutPlaceOrderParams
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

    suspend fun checkPaymentFee(
        ctx: Context,
        cartId: String,
        paymentMethodId: String,
        shippingMethodId: String
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
            .query(
                CheckPaymentProcessingFeeQuery(
                    cartId,
                    paymentMethodId,
                    Optional.presentIfNotNull(shippingMethodId)
                )
            )
            .execute()
        if (resp.hasErrors()) {
            viewState.onCheckPaymentFeeFailure(ApiError())
            return
        }

        viewState.onCheckPaymentFeeSuccess(resp.data!!.checkPaymentProcessingFee)
    }

    suspend fun checkShippingFee(
        ctx: Context,
        cartId: String,
        shippingMethodId: String
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
            .query(CheckShippingChargeQuery(cartId, shippingMethodId))
            .execute()
        if (resp.hasErrors()) {
            viewState.onCheckShippingFeeFailure(ApiError())
            return
        }

        viewState.onCheckShippingFeeSuccess(resp.data!!.checkShippingCharge)
    }

    suspend fun checkDiscount(
        ctx: Context,
        cartId: String,
        couponCode: String,
        shippingMethodId: String
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
            .query(
                CheckDiscountForGuestsQuery(
                    cartId,
                    couponCode,
                    Optional.presentIfNotNull(shippingMethodId)
                )
            )
            .execute()
        if (resp.hasErrors()) {
            viewState.onCheckDiscountFailure(ApiError())
            return
        }

        viewState.onCheckDiscountSuccess(resp.data!!.checkDiscountForGuests)
    }

    suspend fun placeOrder(
        ctx: Context,
        params: GuestCheckoutPlaceOrderParams
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
                OrderGuestCheckoutMutation(params)
            )
            .execute()
        if (resp.hasErrors()) {
            Log.d("Error", Gson().toJson(resp.errors).toString())
            viewState.onPlaceOrderFailure(ApiError())
            return
        }

        viewState.onPlaceOrderSuccess(resp.data!!.orderGuestCheckout)
    }
}
