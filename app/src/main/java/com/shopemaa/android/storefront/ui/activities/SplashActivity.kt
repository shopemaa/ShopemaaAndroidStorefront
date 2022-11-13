package com.shopemaa.android.storefront.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.util.Base64
import android.util.Log
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.gson.Gson
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.shopemaa.android.storefront.R
import com.shopemaa.android.storefront.contants.Constants
import com.shopemaa.android.storefront.models.StoreSecret
import com.shopemaa.android.storefront.storage.CacheStorage
import com.shopemaa.android.storefront.storage.ICacheStorage
import com.shopemaa.android.storefront.utils.JUtil
import com.shopemaa.android.storefront.utils.Utils
import ninja.sakib.kutupicker.activities.GalleryPickerActivity
import ninja.sakib.kutupicker.utils.CodeUtil
import java.io.File
import java.util.*
import kotlin.collections.HashMap


@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val c = CacheStorage(applicationContext)
        parseAndSaveStoreCredential(c)

        val key = c.get(Constants.storeKeyLabel)
        val secret = c.get(Constants.storeSecretLabel)
        if (key.isEmpty() || secret.isEmpty()) {
            val alert = SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            alert.titleText = "Attention"
            alert.contentText = "No store connected. Use QR code to connect to your store."
            alert.confirmText = "Scan QR"
            alert.cancelText = "Read QR"

            alert.setCancelable(false)
            alert.showCancelButton(true)
            alert.setConfirmClickListener {
                startActivity(Intent(this, BarcodeScannerActivity::class.java))
            }
            alert.setCancelClickListener {
                readQRFromFile()
            }
            alert.show()
            return
        }

        startActivity(Intent(this, StoreActivity::class.java))
        finish()
    }

    private fun readQRFromFile() {
        val i = Intent(applicationContext, GalleryPickerActivity::class.java)
        i.putExtra(CodeUtil.MAX_SELECTION, 1)
        i.putExtra(CodeUtil.MIN_SELECTION, 1)
        startActivityForResult(i, CodeUtil.IMAGE_SELECTION_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == CodeUtil.IMAGE_SELECTION_DONE && data != null) {
            val selectedImages = data.getStringArrayListExtra(CodeUtil.SELECTED_IMAGES_KEY)
            if (selectedImages == null || selectedImages.isEmpty()) {
                showMessage(applicationContext, "No QR code selected")
                return
            }

            parseAndSaveStoreCredential(File(selectedImages[0]))
        }
    }

    private fun parseAndSaveStoreCredential(f: File) {
        try {
            val c = CacheStorage(applicationContext)
            val qrText = Utils.parseQRCodeFromImage(f)
            val encodedKey = qrText.replace("shopemaastorefront://", "")
            val decoded = Base64.decode(encodedKey, 0)

            val secret = Gson().fromJson(String(decoded), StoreSecret::class.java)
            if (secret?.key != null && secret.secret != null
                && secret.key!!.isNotEmpty() && secret.secret!!.isNotEmpty()
            ) {
                c.save(Constants.storeKeyLabel, secret.key!!)
                c.save(Constants.storeSecretLabel, secret.secret!!)

                val i = Intent(applicationContext, SplashActivity::class.java)
                startActivity(i)
                finish()
            } else {
                throw Exception("Invalid QR code")
            }
        } catch (e: Exception) {
            showMessage(applicationContext, "Select a valid QR code")
        }
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
