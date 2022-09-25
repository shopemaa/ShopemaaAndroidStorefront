package com.shopemaa.android.storefront.ui.clients

import android.graphics.Bitmap
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.shopemaa.android.storefront.ui.listeners.DigitalPaymentCallback
import com.shopemaa.android.storefront.utils.Utils

class CustomWebViewClient(var callback: DigitalPaymentCallback) : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        if (request != null) {
            if (request.url != null) {
                Log.d("URL", request.url.toString())

                if (request.url.toString().contains(Utils.LocalCallbackSuccessUrl)) {
                    callback.onSuccess()
                }
                if (request.url.toString().contains(Utils.LocalCallbackFailureUrl)) {
                    callback.onFailure()
                }
            }
        }
        return false
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        if (url != null) {
            Log.d("[onPageStarted]sURL", url.toString())

            if (url.toString().contains(Utils.LocalCallbackSuccessUrl)) {
                callback.onSuccess()
            }
            if (url.toString().contains(Utils.LocalCallbackFailureUrl)) {
                callback.onFailure()
            }
        }
        super.onPageStarted(view, url, favicon)
    }
}
