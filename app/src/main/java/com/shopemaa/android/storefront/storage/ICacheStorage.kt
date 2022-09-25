package com.shopemaa.android.storefront.storage

interface ICacheStorage {
    fun save(key: String, value: String): Boolean
    fun get(key: String): String
    fun delete(key: String): Boolean
    fun cleanAll(): Boolean
}
