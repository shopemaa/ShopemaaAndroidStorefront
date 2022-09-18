package com.shopemaa.android.storefront.ui.listeners

import com.shopemaa.android.storefront.api.graphql.CategoriesQuery

interface CategorySelectedListener {
    fun onSelected(c: CategoriesQuery.Category)
}