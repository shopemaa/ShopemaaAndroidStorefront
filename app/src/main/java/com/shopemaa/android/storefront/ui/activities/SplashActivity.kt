package com.shopemaa.android.storefront.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
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
            startActivity(Intent(this, BarcodeScannerActivity::class.java))
            return
        }

        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
