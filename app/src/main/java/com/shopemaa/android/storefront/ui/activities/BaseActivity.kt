package com.shopemaa.android.storefront.ui.activities

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.arellomobile.mvp.MvpAppCompatActivity
import com.shopemaa.android.storefront.R
import com.shopemaa.android.storefront.storage.CacheStorage
import com.shopemaa.android.storefront.storage.ICacheStorage

open class BaseActivity : MvpAppCompatActivity() {
    private val accessTokenKey = "accessToken"

    lateinit var cacheStorage: ICacheStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hideActionBar()
        cacheStorage = CacheStorage(applicationContext)
    }

//    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
//        val v = super.onCreateView(name, context, attrs)
//        cacheStorage = CacheStorage(applicationContext)
//        return v
//    }

    private fun hideActionBar() {
        supportActionBar?.hide()
        actionBar?.hide()
    }

    fun disableAutoFocus() {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    fun isLoggedIn(): Boolean {
        return getAccessToken().isNotEmpty()
    }

    fun getAccessToken(): String {
        return cacheStorage.get(accessTokenKey)
    }

    fun setAccessToken(token: String): Boolean {
        return cacheStorage.save(accessTokenKey, token)
    }

    fun showMessage(ctx: Context, message: String) {
        Toast.makeText(ctx, message, Toast.LENGTH_LONG).show()
    }

    fun createLoader(ctx: Activity, title: String): SweetAlertDialog {
        val alert = SweetAlertDialog(ctx, SweetAlertDialog.PROGRESS_TYPE)
        alert.progressHelper.barColor = ContextCompat.getColor(ctx, R.color.primary)
        alert.titleText = title
        alert.setCancelable(false)
        return alert
    }
}
