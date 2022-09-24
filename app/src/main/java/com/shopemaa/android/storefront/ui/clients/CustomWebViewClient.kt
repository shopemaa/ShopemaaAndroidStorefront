package com.shopemaa.android.storefront.ui.clients

import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.shopemaa.android.storefront.ui.listeners.DigitalPaymentCallback
import com.shopemaa.android.storefront.utils.Utils

class CustomWebViewClient(var callback: DigitalPaymentCallback) : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        if (request != null) {
            if (request.url != null) {
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
}
