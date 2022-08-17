package com.shopemaa.android.storefront.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import cn.pedant.SweetAlert.SweetAlertDialog
import com.shopemaa.android.storefront.R
import com.shopemaa.android.storefront.contants.Constants
import com.shopemaa.android.storefront.storage.CacheStorage

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val c = CacheStorage(applicationContext)
        val key = c.get(Constants.storeKeyLabel)
        val secret = c.get(Constants.storeSecretLabel)
        if (key.isEmpty() || secret.isEmpty()) {
            val alert = SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            alert.titleText = "Attention"
            alert.contentText = "No store connected. Scan QR code to connect to your desired store."
            alert.confirmText = "Scan"
            alert.cancelText = "Cancel"
            alert.setCancelable(false)
            alert.showCancelButton(true)
            alert.setConfirmClickListener {
                startActivity(Intent(this, BarcodeScannerActivity::class.java))
            }
            alert.show()
            return
        }

        startActivity(Intent(this, CheckoutCompleteActivity::class.java))
//        startActivity(Intent(this, StoreActivity::class.java))
        finish()
    }
}
