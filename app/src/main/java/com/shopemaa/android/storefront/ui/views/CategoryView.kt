package com.shopemaa.android.storefront.ui.views

import com.arellomobile.mvp.MvpView
import com.shopemaa.android.storefront.api.graphql.CategoriesQuery
import com.shopemaa.android.storefront.errors.ApiError

interface CategoryView : MvpView {
    fun onCategoriesSuccess(categories: List<CategoriesQuery.Category>)
    fun onCategoriesFailure(err: ApiError)
}
