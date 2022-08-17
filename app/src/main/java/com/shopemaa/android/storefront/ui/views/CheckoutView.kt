package com.shopemaa.android.storefront.ui.views

import com.arellomobile.mvp.MvpView
import com.shopemaa.android.storefront.api.graphql.CountriesQuery
import com.shopemaa.android.storefront.api.graphql.OrderGuestCheckoutMutation
import com.shopemaa.android.storefront.api.graphql.PaymentMethodsQuery
import com.shopemaa.android.storefront.api.graphql.ShippingMethodsQuery
import com.shopemaa.android.storefront.errors.ApiError

interface CheckoutView : MvpView {
    fun onCountryListSuccess(countries: List<CountriesQuery.Location>)
    fun onCountryListFailure(err: ApiError)

    fun onPaymentMethodListSuccess(methods: List<PaymentMethodsQuery.PaymentMethod>)
    fun onPaymentMethodListFailure(err: ApiError)

    fun onShippingMethodListSuccess(methods: List<ShippingMethodsQuery.ShippingMethod>)
    fun onShippingMethodListFailure(err: ApiError)

    fun onCheckShippingFeeSuccess(amount: Int)
    fun onCheckShippingFeeFailure(err: ApiError)

    fun onCheckPaymentFeeSuccess(amount: Int)
    fun onCheckPaymentFeeFailure(err: ApiError)

    fun onCheckDiscountSuccess(amount: Int)
    fun onCheckDiscountFailure(err: ApiError)

    fun onPlaceOrderSuccess(order: OrderGuestCheckoutMutation.OrderGuestCheckout)
    fun onPlaceOrderFailure(err: ApiError)
}