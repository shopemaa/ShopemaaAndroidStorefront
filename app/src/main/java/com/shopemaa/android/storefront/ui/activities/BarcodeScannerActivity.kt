package com.shopemaa.android.storefront.ui.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.app.ActivityCompat
import androidx.core.util.forEach
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.shopemaa.android.storefront.R
import com.shopemaa.android.storefront.contants.Constants
import com.shopemaa.android.storefront.models.StoreSecret

class BarcodeScannerActivity : BaseActivity(), PermissionListener {
    private lateinit var surfaceView: SurfaceView
    private lateinit var cameraSource: CameraSource
    private lateinit var detector: BarcodeDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_scanner)

        surfaceView = findViewById(R.id.surfaceView)

        requestPermission()
    }

    private fun startScanner() {
        detector = BarcodeDetector.Builder(applicationContext)
            .setBarcodeFormats(Barcode.ALL_FORMATS)
            .build()

        if (!detector.isOperational) {
            finish()
            return
        }

        cameraSource = CameraSource.Builder(this, detector)
            .setRequestedPreviewSize(1920, 1080)
            .setAutoFocusEnabled(true)
            .build()

        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(p0: SurfaceHolder) {
                if (ActivityCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.CAMERA
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // Should not happen
                    return
                }

                cameraSource.start(surfaceView.holder)
            }

            override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {

            }

            override fun surfaceDestroyed(p0: SurfaceHolder) {
                cameraSource.stop()
            }
        })

        detector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {

            }

            override fun receiveDetections(p0: Detector.Detections<Barcode>) {
                val barcodes = p0.detectedItems
                if (barcodes.size() > 0) {
                    barcodes.forEach { key, value ->
                        val encodedKey = value.displayValue.replace("shopemaastorefront://", "")
                        val decoded = Base64.decode(encodedKey, 0)
                        val secret = Gson().fromJson(String(decoded), StoreSecret::class.java)

                        if (secret?.key != null && secret.secret != null
                            && secret.key!!.isNotEmpty() && secret.secret!!.isNotEmpty()
                        ) {
                            val c = getCacheStorage(applicationContext)
                            c.cleanAll()

                            c.save(Constants.storeKeyLabel, secret.key!!)
                            c.save(Constants.storeSecretLabel, secret.secret!!)

                            startActivity(Intent(applicationContext, SplashActivity::class.java))
                            finish()
                        }
                    }
                }
            }
        })
    }

    private fun requestPermission() {
        Dexter
            .withActivity(this)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(this)
            .check()
    }

    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
        startScanner()
    }

    override fun onPermissionDenied(response: PermissionDeniedResponse?) {
        showMessage(applicationContext, "Camera permission required to scan barcode.")
    }

    override fun onPermissionRationaleShouldBeShown(
        permission: PermissionRequest?,
        token: PermissionToken?
    ) {

    }

    override fun onPause() {
        super.onPause()
        try {
            cameraSource.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
