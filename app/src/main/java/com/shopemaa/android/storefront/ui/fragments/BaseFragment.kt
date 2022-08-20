package com.shopemaa.android.storefront.ui.fragments

import android.app.Activity
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.arellomobile.mvp.MvpAppCompatFragment
import com.shopemaa.android.storefront.R
import com.shopemaa.android.storefront.models.PowerSpinnerModel
import com.shopemaa.android.storefront.storage.CacheStorage
import com.shopemaa.android.storefront.storage.ICacheStorage
import com.shopemaa.android.storefront.ui.adapters.TwoFieldDropdownAdapter
import com.shopemaa.android.storefront.utils.Utils
import com.skydoves.powerspinner.PowerSpinnerView

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

    fun createCustomPopup(
        ctx: Activity,
        v: View,
        listener: SweetAlertDialog.OnSweetClickListener
    ): SweetAlertDialog {
        return createCustomPopup(ctx, "", v, "Ok", true, listener)
    }

    fun createCustomPopup(
        ctx: Activity,
        title: String,
        v: View,
        confirmBtnTxt: String,
        isCancelable: Boolean,
        listener: SweetAlertDialog.OnSweetClickListener
    ): SweetAlertDialog {
        val alert = SweetAlertDialog(ctx, SweetAlertDialog.NORMAL_TYPE)
        if (title.isEmpty().not()) {
            alert.titleText = title
        }
        alert.setCustomView(v)
        alert.setCancelable(isCancelable)
        alert.confirmText = confirmBtnTxt
        alert.setConfirmClickListener(listener)
        return alert
    }

    fun createSpinner(
        ctx: Context,
        name: String,
        listener: TwoFieldDropdownAdapter.OnHolderItemSelectedListener,
        values: List<PowerSpinnerModel>
    ): PowerSpinnerView {
        val spinner = PowerSpinnerView(ctx)
        spinner.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
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
