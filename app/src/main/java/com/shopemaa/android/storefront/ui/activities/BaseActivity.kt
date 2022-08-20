package com.shopemaa.android.storefront.ui.activities

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.arellomobile.mvp.MvpAppCompatActivity
import com.shopemaa.android.storefront.R
import com.shopemaa.android.storefront.models.PowerSpinnerModel
import com.shopemaa.android.storefront.storage.CacheStorage
import com.shopemaa.android.storefront.storage.ICacheStorage
import com.shopemaa.android.storefront.ui.adapters.TwoFieldDropdownAdapter
import com.shopemaa.android.storefront.utils.Utils
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener
import com.skydoves.powerspinner.PowerSpinnerView

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

    fun getCacheStorage(ctx: Context): ICacheStorage {
        return cacheStorage
    }

    fun createLoader(ctx: Activity, title: String): SweetAlertDialog {
        val alert = SweetAlertDialog(ctx, SweetAlertDialog.PROGRESS_TYPE)
        alert.progressHelper.barColor = ContextCompat.getColor(ctx, R.color.primary)
        alert.titleText = title
        alert.setCancelable(false)
        return alert
    }

    fun createCustomPopup(
        ctx: Activity,
        v: View,
        listener: SweetAlertDialog.OnSweetClickListener
    ): SweetAlertDialog {
        val alert = SweetAlertDialog(ctx, SweetAlertDialog.NORMAL_TYPE)
        alert.setCustomView(v)
        alert.setCancelable(true)
        alert.confirmText = "Filter"
        alert.setConfirmClickListener(listener)
        return alert
    }

    fun createOverlayLoader(activity: Activity, ctx: Context, title: String): SweetAlertDialog {
        val alert = SweetAlertDialog(activity, SweetAlertDialog.NORMAL_TYPE)
//        alert.progressHelper.barColor = ContextCompat.getColor(activity, R.color.primary)
//        alert.titleText = title

        alert.hideConfirmButton()
        val v = LayoutInflater.from(ctx).inflate(R.layout.loading_overlay, null)
        alert.setCustomView(v)

        alert.setCancelable(false)
        return alert
    }

    fun createSpinner(
        ctx: Context,
        name: String,
        listener: TwoFieldDropdownAdapter.OnHolderItemSelectedListener,
        values: List<PowerSpinnerModel>
    ): PowerSpinnerView {
        val spinner = PowerSpinnerView(ctx)
        spinner.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 30)
        spinner.hint = name
        spinner.textSize = 18f
        spinner.gravity = Gravity.CENTER
        val adapter = TwoFieldDropdownAdapter(0, null, spinner)
        spinner.setSpinnerAdapter(adapter)
        spinner.spinnerPopupHeight = Utils.getScreenHeightDp(ctx) / 2
        adapter.setListener(listener)
        adapter.setItems(values)
        return spinner
    }
}
