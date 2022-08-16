package com.shopemaa.android.storefront.ui.fragments

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.core.content.ContextCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.arellomobile.mvp.MvpAppCompatFragment
import com.shopemaa.android.storefront.R
import com.shopemaa.android.storefront.storage.CacheStorage
import com.shopemaa.android.storefront.storage.ICacheStorage

open class BaseFragment : MvpAppCompatFragment() {

    fun getCacheStorage(ctx: Context): ICacheStorage {
        return CacheStorage(ctx)
    }

    fun createLoader(ctx: Activity, title: String): SweetAlertDialog {
        val alert = SweetAlertDialog(ctx, SweetAlertDialog.PROGRESS_TYPE)
        alert.progressHelper.barColor = ContextCompat.getColor(ctx, R.color.primary)
        alert.titleText = title
        alert.setCancelable(false)
        return alert
    }

    fun showMessage(ctx: Context, message: String) {
        Toast.makeText(ctx, message, Toast.LENGTH_LONG).show()
    }
}
