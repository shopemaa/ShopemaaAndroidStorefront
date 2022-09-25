package com.shopemaa.android.storefront.storage

import android.content.Context
import android.content.SharedPreferences

class CacheStorage(ctx: Context) : ICacheStorage {
    private var store: SharedPreferences = ctx.getSharedPreferences("ShopemaaAndroidStorefront", 0)

    override fun save(key: String, value: String): Boolean {
        val editor = store.edit()
        editor.putString(key, value)
        return editor.commit()
    }

    override fun get(key: String): String {
        return store.getString(key, "")!!
    }

    override fun delete(key: String): Boolean {
        val editor = store.edit()
        editor.remove(key)
        return editor.commit()
    }

    override fun cleanAll(): Boolean {
        return store.edit().clear().commit()
    }
}
