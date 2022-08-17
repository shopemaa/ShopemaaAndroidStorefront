package com.shopemaa.android.storefront.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import cn.pedant.SweetAlert.SweetAlertDialog
import com.arellomobile.mvp.presenter.InjectPresenter
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.rengwuxian.materialedittext.MaterialEditText
import com.shopemaa.android.storefront.R
import com.shopemaa.android.storefront.api.graphql.CartQuery
import com.shopemaa.android.storefront.api.graphql.CountriesQuery
import com.shopemaa.android.storefront.api.graphql.PaymentMethodsQuery
import com.shopemaa.android.storefront.api.graphql.ShippingMethodsQuery
import com.shopemaa.android.storefront.contants.Constants
import com.shopemaa.android.storefront.errors.ApiError
import com.shopemaa.android.storefront.ui.presenters.CheckoutPresenter
import com.shopemaa.android.storefront.ui.views.CheckoutView
import com.shopemaa.android.storefront.utils.CartUtil
import com.shopemaa.android.storefront.utils.Utils
import kotlinx.coroutines.launch

class CheckoutCompleteActivity : BaseActivity(), CheckoutView {
    private lateinit var firstName: EditText
    private lateinit var lastName: EditText
    private lateinit var email: EditText
    private lateinit var phone: EditText
    private lateinit var address: EditText
    private lateinit var shippingMethod: TextView
    private lateinit var paymentMethod: TextView
    private lateinit var subtotal: TextView
    private lateinit var shippingFee: TextView
    private lateinit var paymentFee: TextView
    private lateinit var discount: TextView
    private lateinit var grandTotal: TextView

    private lateinit var couponCode: MaterialEditText
    private lateinit var applyCouponBtn: MaterialButton
    private lateinit var placeOrderBtn: MaterialButton

    private lateinit var backBtn: ImageView

    private lateinit var country: CountriesQuery.Location
    private lateinit var shippingMethodV: ShippingMethodsQuery.ShippingMethod
    private lateinit var paymentMethodV: PaymentMethodsQuery.PaymentMethod

    private lateinit var cart: CartQuery.Cart

    @InjectPresenter
    lateinit var presenter: CheckoutPresenter
    lateinit var alertDialog: SweetAlertDialog

    private var calSubtotal: Int = 0
    private var calShippingFee: Int = 0
    private var calPaymentFee: Int = 0
    private var calDiscount: Int = 0

    /**
     * Order
     * 0. Subtotal
     * 1. Shipping Fee
     * 2. Payment Fee
     * 3. Discount
     * 4. Grand Total
     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout_complete)

        firstName = findViewById(R.id.checkout_complete_first_name)
        lastName = findViewById(R.id.checkout_complete_last_name)
        email = findViewById(R.id.checkout_complete_email)
        phone = findViewById(R.id.checkout_complete_phone)
        address = findViewById(R.id.checkout_complete_shipping_address)
        shippingMethod = findViewById(R.id.shipping_method)
        paymentMethod = findViewById(R.id.payment_method)
        subtotal = findViewById(R.id.subtotal)
        shippingFee = findViewById(R.id.shipping_fee)
        paymentFee = findViewById(R.id.payment_fee)
        discount = findViewById(R.id.discount)
        grandTotal = findViewById(R.id.grand_total)

        backBtn = findViewById(R.id.checkout_complete_view_back)
        backBtn.setOnClickListener {
            onBackPressed()
        }

        couponCode = findViewById(R.id.coupon_code)
        applyCouponBtn = findViewById(R.id.apply_coupon_btn)
        applyCouponBtn.setOnClickListener {
            applyCoupon()
        }
        placeOrderBtn = findViewById(R.id.place_order_btn)
        placeOrderBtn.setOnClickListener {
            placeOrder()
        }

        bindValues()
    }

    @SuppressLint("SetTextI18n")
    private fun bindValues() {
        val c = getCacheStorage(applicationContext)

        cart = CartUtil.cartFromCache(c)

        country = Gson().fromJson(
            c.get(Constants.countryLabel),
            CountriesQuery.Location::class.java
        )
        shippingMethodV = Gson().fromJson(
            c.get(Constants.shippingMethodLabel),
            ShippingMethodsQuery.ShippingMethod::class.java
        )
        paymentMethodV = Gson().fromJson(
            c.get(Constants.paymentMethodLabel),
            PaymentMethodsQuery.PaymentMethod::class.java
        )

        firstName.setText(c.get(Constants.firstNameLabel))
        lastName.setText(c.get(Constants.lastNameLabel))
        email.setText(c.get(Constants.emailLabel))
        phone.setText(c.get(Constants.phoneLabel))
        address.setText(
            "${c.get(Constants.streetLabel)}, ${c.get(Constants.street2Label)}, ${c.get(Constants.postcodeLabel)}, ${
                c.get(
                    Constants.cityLabel
                )
            }, ${
                c.get(
                    Constants.stateLabel
                )
            }, ${country.name}"
        )

        paymentMethod.text = "${paymentMethodV.displayName} (${paymentMethodV.currencyName})"

        shippingMethod.text =
            "${shippingMethodV.displayName} - Approx. delivery in ${shippingMethodV.deliveryTimeInDays} days"

        calculateOverview()
    }

    private fun calculateOverview() {
        if (::alertDialog.isInitialized.not()) {
            alertDialog = createLoader(this, "Please wait...")
        }

        if (alertDialog.isShowing.not()) {
            alertDialog.show()
        }

        var st = 0
        cart.cartItems.forEach {
            st += it.purchasePrice * it.quantity
        }
        subtotal.text = Utils.formatAmount(st, true)
        calSubtotal = st

        lifecycleScope.launch {
            presenter.checkShippingFee(applicationContext, cart.id, shippingMethodV.id)
        }
    }

    private fun applyCoupon() {
        if (couponCode.text?.trim()!!.isEmpty()) {
            return
        }

        alertDialog = createLoader(this, "Please wait...")
        alertDialog.show()

        lifecycleScope.launch {
            presenter.checkDiscount(
                applicationContext,
                cart.id,
                couponCode.text.toString(),
                shippingMethodV.id
            )
        }
    }

    private fun placeOrder() {

    }

    override fun onShippingMethodListSuccess(methods: List<ShippingMethodsQuery.ShippingMethod>) {

    }

    override fun onShippingMethodListFailure(err: ApiError) {

    }

    override fun onPaymentMethodListSuccess(methods: List<PaymentMethodsQuery.PaymentMethod>) {

    }

    override fun onPaymentMethodListFailure(err: ApiError) {

    }

    override fun onCountryListSuccess(countries: List<CountriesQuery.Location>) {

    }

    override fun onCountryListFailure(err: ApiError) {

    }

    override fun onCheckShippingFeeSuccess(amount: Int) {
        calShippingFee = amount
        shippingFee.text = Utils.formatAmount(amount, true)

        lifecycleScope.launch {
            presenter.checkPaymentFee(
                applicationContext,
                cart.id,
                paymentMethodV.id,
                shippingMethodV.id
            )
        }
    }

    override fun onCheckShippingFeeFailure(err: ApiError) {
        lifecycleScope.launch {
            presenter.checkShippingFee(applicationContext, cart.id, shippingMethodV.id)
        }
    }

    override fun onCheckPaymentFeeSuccess(amount: Int) {
        calPaymentFee = amount
        paymentFee.text = Utils.formatAmount(amount, true)
        calculateGrandTotal()
    }

    override fun onCheckPaymentFeeFailure(err: ApiError) {
        lifecycleScope.launch {
            presenter.checkPaymentFee(
                applicationContext,
                cart.id,
                paymentMethodV.id,
                shippingMethodV.id
            )
        }
    }

    override fun onCheckDiscountSuccess(amount: Int) {
        calDiscount = amount
        discount.text = Utils.formatAmount(amount, true)

        calculateOverview()
    }

    override fun onCheckDiscountFailure(err: ApiError) {
        showMessage(applicationContext, "Failed to apply coupon")
        calDiscount = 0
        discount.text = Utils.formatAmount(0, true)

        calculateOverview()
    }

    private fun calculateGrandTotal() {
        grandTotal.text = Utils.formatAmount(
            calSubtotal + calPaymentFee + calShippingFee - calDiscount,
            true
        )

        alertDialog.dismiss()
    }

    override fun onBackPressed() {
        startActivity(Intent(applicationContext, CheckoutActivity::class.java))
        finish()
    }
}
