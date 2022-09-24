package com.shopemaa.android.storefront.ui.views

import com.arellomobile.mvp.MvpView
import com.shopemaa.android.storefront.api.graphql.OrderGeneratePaymentNonceForGuestMutation
import com.shopemaa.android.storefront.errors.ApiError

interface DigitalPaymentView : MvpView {
    fun generatePaymentNonceSuccess(result: OrderGeneratePaymentNonceForGuestMutation.OrderGeneratePaymentNonceForGuest)
    fun generatePaymentNonceFailure(err: ApiError)
}
