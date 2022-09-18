package com.shopemaa.android.storefront.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.gson.Gson
import com.shopemaa.android.storefront.R
import com.shopemaa.android.storefront.contants.Constants
import com.shopemaa.android.storefront.models.StoreSecret
import com.shopemaa.android.storefront.storage.CacheStorage
import com.shopemaa.android.storefront.storage.ICacheStorage

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val c = CacheStorage(applicationContext)
        parseAndSaveStoreCredential(c)

        c.save(
            Constants.storeKeyLabel,
            "779ab8d53c0c4de186a729b64dd160a580da51cc2e3b491391623a0c886762111257ca7ef455410d973926f77084b75d"
        )
        c.save(
            Constants.storeSecretLabel,
            "27f173af88b743018c507199688afb529bceb47f17214f8d9c72ee0b6bf4a20e"
        )

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

        startActivity(Intent(this, StoreActivity::class.java))
        finish()
    }

    private fun parseAndSaveStoreCredential(c: ICacheStorage) {
        if (intent.data == null || intent.data.toString().trim().isEmpty()) {
            return
        }

        val encodedKey = intent.data.toString().replace("shopemaastorefront://", "")
        val decoded = Base64.decode(encodedKey, 0)

        val secret = Gson().fromJson(String(decoded), StoreSecret::class.java)
        if (secret?.key != null && secret.secret != null
            && secret.key!!.isNotEmpty() && secret.secret!!.isNotEmpty()
        ) {
            c.save(Constants.storeKeyLabel, secret.key!!)
            c.save(Constants.storeSecretLabel, secret.secret!!)
        }
    }
}
