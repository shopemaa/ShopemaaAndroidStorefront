package com.shopemaa.android.storefront.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import cn.pedant.SweetAlert.SweetAlertDialog
import com.apollographql.apollo3.api.or
import com.arellomobile.mvp.presenter.InjectPresenter
import com.google.android.material.button.MaterialButton
import com.shopemaa.android.storefront.R
import com.shopemaa.android.storefront.api.graphql.OrderByCustomerEmailQuery
import com.shopemaa.android.storefront.api.graphql.type.OrderPaymentStatus
import com.shopemaa.android.storefront.contants.Constants
import com.shopemaa.android.storefront.errors.ApiError
import com.shopemaa.android.storefront.ui.adapters.OrderItemListAdapter
import com.shopemaa.android.storefront.ui.presenters.OrderPresenter
import com.shopemaa.android.storefront.ui.views.OrderView
import com.shopemaa.android.storefront.utils.Utils
import kotlinx.coroutines.launch

class OrderDetailsActivity : BaseActivity(), OrderView {

    @InjectPresenter
    lateinit var presenter: OrderPresenter
    private lateinit var alertDialog: SweetAlertDialog

    private lateinit var orderHash: TextView
    private lateinit var orderStatus: TextView
    private lateinit var orderDate: TextView
    private lateinit var orderPaymentStatus: TextView
    private lateinit var customerName: TextView
    private lateinit var customerEmail: TextView
    private lateinit var customerPhone: TextView
    private lateinit var customerAddress: TextView
    private lateinit var shippingMethod: TextView
    private lateinit var paymentMethod: TextView
    private lateinit var coupon: TextView
    private lateinit var subtotal: TextView
    private lateinit var shippingFee: TextView
    private lateinit var paymentFee: TextView
    private lateinit var discount: TextView
    private lateinit var grandTotal: TextView

    private lateinit var back: ImageView

    private lateinit var orderItems: RecyclerView
    private lateinit var orderItemsList: MutableList<OrderByCustomerEmailQuery.CartItem>
    private lateinit var orderItemsAdapter: OrderItemListAdapter

    private lateinit var payNowBtn: MaterialButton

    private lateinit var order: OrderByCustomerEmailQuery.OrderByCustomerEmail

    private lateinit var swipeLayout: SwipeRefreshLayout

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

        payNowBtn = findViewById(R.id.order_details_pay_btn)
        payNowBtn.visibility = View.GONE
        payNowBtn.setOnClickListener {
            if (::order.isInitialized) {
                val i = Intent(applicationContext, DigitalPaymentActivity::class.java)
                i.putExtra(Constants.orderHashLabel, order.hash)
                i.putExtra(Constants.orderCustomerEmailLabel, order.customer.email)
                i.putExtra(Constants.orderIdLabel, order.id)
                startActivity(i)
                finish()
            }
        }

        swipeLayout = findViewById(R.id.order_details_layout)
        swipeLayout.setOnRefreshListener {
            if (::order.isInitialized) {
                alertDialog.show()

                lifecycleScope.launch {
                    presenter.requestOrderDetails(
                        applicationContext,
                        order.hash,
                        order.customer.email
                    )
                }
            }
        }

        orderItems = findViewById(R.id.order_items)
        orderItemsList = mutableListOf()
        orderItemsAdapter = OrderItemListAdapter(applicationContext, orderItemsList)
        val layoutManager = LinearLayoutManager(applicationContext, RecyclerView.VERTICAL, false)
        orderItems.layoutManager = layoutManager
        orderItems.adapter = orderItemsAdapter

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

        back = findViewById(R.id.order_details_view_back)
        back.setOnClickListener {
            finish()
        }
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onOrderDetailsSuccess(order: OrderByCustomerEmailQuery.OrderByCustomerEmail) {
        this.order = order

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

        subtotal.text = Utils.formatAmount(applicationContext, order.subtotal, true)
        shippingFee.text = Utils.formatAmount(applicationContext, order.shippingCharge, true)
        paymentFee.text = Utils.formatAmount(applicationContext, order.paymentProcessingFee, true)
        discount.text = Utils.formatAmount(applicationContext, order.discountedAmount, true)
        grandTotal.text = Utils.formatAmount(applicationContext, order.grandTotal, true)

        orderItemsList.clear()
        orderItemsList.addAll(order.cart.cartItems)
        orderItemsAdapter.notifyDataSetChanged()

        if (order.paymentStatus != OrderPaymentStatus.Paid) {
            payNowBtn.visibility = View.VISIBLE
        }

        alertDialog.dismiss()
        swipeLayout.isRefreshing = false
    }

    override fun onOrderDetailsFailure(err: ApiError) {
        alertDialog.dismiss()
        showMessage(applicationContext, "Order not found")
        finish()
        swipeLayout.isRefreshing = false
    }

    override fun onBackPressed() {
        finish()
    }
}
