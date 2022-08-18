package com.shopemaa.android.storefront.ui.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import cn.pedant.SweetAlert.SweetAlertDialog
import com.arellomobile.mvp.presenter.InjectPresenter
import com.shopemaa.android.storefront.R
import com.shopemaa.android.storefront.api.graphql.OrderByCustomerEmailQuery
import com.shopemaa.android.storefront.contants.Constants
import com.shopemaa.android.storefront.errors.ApiError
import com.shopemaa.android.storefront.ui.presenters.OrderPresenter
import com.shopemaa.android.storefront.ui.views.OrderView
import com.shopemaa.android.storefront.utils.Utils
import kotlinx.coroutines.launch

class OrderDetailsActivity : BaseActivity(), OrderView {

    @InjectPresenter
    lateinit var presenter: OrderPresenter
    lateinit var alertDialog: SweetAlertDialog

    lateinit var orderHash: TextView
    lateinit var orderStatus: TextView
    lateinit var orderDate: TextView
    lateinit var orderPaymentStatus: TextView
    lateinit var customerName: TextView
    lateinit var customerEmail: TextView
    lateinit var customerPhone: TextView
    lateinit var customerAddress: TextView
    lateinit var shippingMethod: TextView
    lateinit var paymentMethod: TextView
    lateinit var coupon: TextView
    lateinit var subtotal: TextView
    lateinit var shippingFee: TextView
    lateinit var paymentFee: TextView
    lateinit var discount: TextView
    lateinit var grandTotal: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_details)

        alertDialog = createLoader(this, "Please wait...")
        alertDialog.show()

        orderHash = findViewById(R.id.order_hash)
        orderStatus = findViewById(R.id.order_status)
        orderDate = findViewById(R.id.order_date)
        orderPaymentStatus = findViewById(R.id.order_payment_status)
        customerName = findViewById(R.id.order_contact_name)
        customerEmail = findViewById(R.id.order_contact_email)
        customerPhone = findViewById(R.id.order_contact_phone)
        customerAddress = findViewById(R.id.order_shipping_address)
        shippingMethod = findViewById(R.id.order_shipping_method)
        paymentMethod = findViewById(R.id.order_payment_method)
        coupon = findViewById(R.id.coupon_code)
        subtotal = findViewById(R.id.subtotal)
        shippingFee = findViewById(R.id.shipping_fee)
        paymentFee = findViewById(R.id.payment_fee)
        discount = findViewById(R.id.discount)
        grandTotal = findViewById(R.id.grand_total)

        val extras = intent.extras
        if (extras != null) {
            val h = extras.getString(Constants.orderHashLabel, "")
            val e = extras.getString(Constants.orderCustomerEmailLabel, "")

            if (h.trim().isNotEmpty() && e.trim().isNotEmpty()) {
                lifecycleScope.launch {
                    presenter.requestOrderDetails(applicationContext, h, e)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onOrderDetailsSuccess(order: OrderByCustomerEmailQuery.OrderByCustomerEmail) {
        orderHash.text = "#${order.hash}"
        orderStatus.text = "Order is ${order.status}"
        orderDate.text = order.createdAt
        orderPaymentStatus.text = "Payment is ${order.paymentStatus}"
        customerName.text = "${order.customer.firstName} ${order.customer.lastName}"
        customerEmail.text = order.customer.email
        customerPhone.text = order.shippingAddress!!.phone
        customerAddress.text =
            "${order.shippingAddress.street}, ${order.shippingAddress.streetTwo}, ${order.shippingAddress.city}, ${order.shippingAddress.state}, ${order.shippingAddress.postcode}, ${order.shippingAddress.location.name}"
        shippingMethod.text =
            "${order.shippingMethod!!.displayName} (Approx. delivery in ${order.shippingMethod.deliveryTimeInDays} days)"
        paymentMethod.text =
            "${order.paymentMethod!!.displayName} (${order.paymentMethod.currencyName})"

        if (order.couponCode != null) {
            coupon.text = "Coupon applied ${order.couponCode.code}"
        } else {
            coupon.visibility = View.INVISIBLE
        }

        subtotal.text = Utils.formatAmount(order.subtotal, true)
        shippingFee.text = Utils.formatAmount(order.shippingCharge, true)
        paymentFee.text = Utils.formatAmount(order.paymentProcessingFee, true)
        discount.text = Utils.formatAmount(order.discountedAmount, true)
        grandTotal.text = Utils.formatAmount(order.grandTotal, true)

        alertDialog.dismiss()
    }

    override fun onOrderDetailsFailure(err: ApiError) {
        alertDialog.dismiss()
        showMessage(applicationContext, "Order not found")
        finish()
    }

    override fun onBackPressed() {
        finish()
    }
}
