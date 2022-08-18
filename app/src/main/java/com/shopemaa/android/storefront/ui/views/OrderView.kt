package com.shopemaa.android.storefront.ui.views

import com.arellomobile.mvp.MvpView
import com.shopemaa.android.storefront.api.graphql.OrderByCustomerEmailQuery
import com.shopemaa.android.storefront.errors.ApiError

interface OrderView : MvpView {
    fun onOrderDetailsSuccess(order: OrderByCustomerEmailQuery.OrderByCustomerEmail)
    fun onOrderDetailsFailure(err: ApiError)
}