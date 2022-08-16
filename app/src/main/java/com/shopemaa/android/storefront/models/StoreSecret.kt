package com.shopemaa.android.storefront.models

import com.google.gson.annotations.SerializedName

class StoreSecret {
    @SerializedName("key")
    var key: String? = null

    @SerializedName("secret")
    var secret: String? = null

    override fun toString(): String {
        return "{key: ${key}, value: ${secret}}"
    }
}