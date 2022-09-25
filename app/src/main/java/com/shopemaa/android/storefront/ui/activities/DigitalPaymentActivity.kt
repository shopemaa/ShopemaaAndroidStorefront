package com.shopemaa.android.storefront.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebView
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import cn.pedant.SweetAlert.SweetAlertDialog
import com.apollographql.apollo3.api.Optional
import com.arellomobile.mvp.presenter.InjectPresenter
import com.shopemaa.android.storefront.R
import com.shopemaa.android.storefront.api.graphql.OrderGeneratePaymentNonceForGuestMutation
import com.shopemaa.android.storefront.api.graphql.type.PaymentRequestOverrides
import com.shopemaa.android.storefront.contants.Constants
import com.shopemaa.android.storefront.errors.ApiError
import com.shopemaa.android.storefront.ui.clients.CustomWebViewClient
import com.shopemaa.android.storefront.ui.listeners.DigitalPaymentCallback
import com.shopemaa.android.storefront.ui.presenters.DigitalPaymentPresenter
import com.shopemaa.android.storefront.ui.views.DigitalPaymentView
import com.shopemaa.android.storefront.utils.Utils
import kotlinx.coroutines.launch

class DigitalPaymentActivity : BaseActivity(), DigitalPaymentView, DigitalPaymentCallback {
    private lateinit var orderHash: String
    private lateinit var customerEmail: String
    private lateinit var orderId: String

    private lateinit var webView: WebView
    private lateinit var goBackView: ImageView

    lateinit var alertDialog: SweetAlertDialog
    lateinit var gatewayResult: OrderGeneratePaymentNonceForGuestMutation.OrderGeneratePaymentNonceForGuest

    @InjectPresenter
    lateinit var presenter: DigitalPaymentPresenter

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_digital_payment)

        alertDialog = createLoader(this, "Please wait...")
        alertDialog.show()

        CookieManager.getInstance().setAcceptCookie(true)

        goBackView = findViewById(R.id.payment_view_back)
        goBackView.setOnClickListener {
            gotoOrderDetails()
        }

        webView = findViewById(R.id.payment_view)
        webView.webViewClient = CustomWebViewClient(this)
        val settings = webView.settings
        settings.javaScriptEnabled = true

        initPayment()
    }

    private fun initPayment() {
        val extras = intent.extras
        if (extras != null) {
            val h = extras.getString(Constants.orderHashLabel, "")
            val e = extras.getString(Constants.orderCustomerEmailLabel, "")
            val id = extras.getString(Constants.orderIdLabel, "")

            if (h.trim().isNotEmpty() && e.trim().isNotEmpty() && e.trim().isNotEmpty()) {
                customerEmail = e
                orderHash = h
                orderId = id

                generateNonce()
            } else {
                showMessage(applicationContext, "Required fields invalid")
            }
        } else {
            showMessage(applicationContext, "Required fields missing")
        }
    }

    private fun generateNonce() {
        lifecycleScope.launch {
            presenter.requestPaymentNonce(
                applicationContext,
                orderId,
                customerEmail,
                PaymentRequestOverrides(
                    Optional.presentIfNotNull(Utils.LocalCallbackSuccessUrl),
                    Optional.presentIfNotNull(Utils.LocalCallbackFailureUrl)
                )
            )
        }
    }

    private fun loadView() {
        webView.loadUrl(
            String.format(
                "https://shopemaa.com/dummy/stripe/checkout/?key=%s&nonce=%s",
                gatewayResult.StripePublishableKey,
                gatewayResult.Nonce
            )
        )
        alertDialog.dismiss()
        webView.visibility = View.VISIBLE
    }

    private fun loadSSLCommerzView() {
        webView.loadUrl(
            gatewayResult.Nonce
        )
        alertDialog.dismiss()
        webView.visibility = View.VISIBLE
    }

    override fun generatePaymentNonceSuccess(result: OrderGeneratePaymentNonceForGuestMutation.OrderGeneratePaymentNonceForGuest) {
        this.gatewayResult = result

        if (this.gatewayResult.PaymentGatewayName == "SSLCommerz") {
            loadSSLCommerzView()
        } else if (this.gatewayResult.PaymentGatewayName == "Stripe") {
            loadView()
        } else {
            showMessage(applicationContext, "Unsupported payment gateway")
        }
    }

    override fun generatePaymentNonceFailure(err: ApiError) {
        showMessage(applicationContext, "Failed to generate nonce")
        alertDialog.show()
    }

    override fun onSuccess() {
        showMessage(applicationContext, "Payment successful")
        gotoOrderDetails()
    }

    private fun gotoOrderDetails() {
        if (::orderHash.isInitialized.not() || ::customerEmail.isInitialized.not()) {
            return
        }

        val i = Intent(applicationContext, OrderDetailsActivity::class.java)
        i.putExtra(Constants.orderHashLabel, orderHash)
        i.putExtra(Constants.orderCustomerEmailLabel, customerEmail)
        startActivity(i)
        finish()
    }

    override fun onFailure() {
        webView.visibility = View.INVISIBLE
        showMessage(applicationContext, "Payment failed")
        generateNonce()
        alertDialog.show()
    }

    override fun onBackPressed() {
        gotoOrderDetails()
    }
}
