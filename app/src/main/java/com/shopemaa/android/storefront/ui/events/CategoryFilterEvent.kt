package com.shopemaa.android.storefront.ui.events

import com.shopemaa.android.storefront.api.graphql.CategoriesQuery

class CategoryFilterEvent(var category: CategoriesQuery.Category) {
}