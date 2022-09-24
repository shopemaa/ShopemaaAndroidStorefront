package com.shopemaa.android.storefront.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import cn.pedant.SweetAlert.SweetAlertDialog
import com.apollographql.apollo3.api.Optional
import com.arellomobile.mvp.presenter.InjectPresenter
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.rengwuxian.materialedittext.MaterialEditText
import com.shopemaa.android.storefront.R
import com.shopemaa.android.storefront.api.graphql.*
import com.shopemaa.android.storefront.api.graphql.type.AddressParams
import com.shopemaa.android.storefront.api.graphql.type.GuestCheckoutPlaceOrderParams
import com.shopemaa.android.storefront.contants.Constants
import com.shopemaa.android.storefront.errors.ApiError
import com.shopemaa.android.storefront.storage.CacheStorage
import com.shopemaa.android.storefront.ui.presenters.CheckoutPresenter
import com.shopemaa.android.storefront.ui.presenters.DigitalPaymentPresenter
import com.shopemaa.android.storefront.ui.views.CheckoutView
import com.shopemaa.android.storefront.ui.views.DigitalPaymentView
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

    private lateinit var order: OrderGuestCheckoutMutation.OrderGuestCheckout

    @InjectPresenter
    lateinit var presenter: CheckoutPresenter

    lateinit var alertDialog: SweetAlertDialog

    private var calSubtotal: Int = 0
    private var calShippingFee: Int = 0
    private var calPaymentFee: Int = 0
    private var calDiscount: Int = 0
    private var calProductSpecificDiscount = 0

    /**
     * Execution Order
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
        calProductSpecificDiscount = 0
        cart.cartItems.forEach {
            st += if (it.product.productSpecificDiscount > 0) {
                val discountedPrice = Utils.discountedPrice(
                    it.product.productSpecificDiscount,
                    it.purchasePrice
                ) * it.quantity
                calProductSpecificDiscount += it.purchasePrice - discountedPrice
                discountedPrice
            } else {
                it.purchasePrice * it.quantity
            }
        }
        subtotal.text = Utils.formatAmount(applicationContext, st, true)
        calSubtotal = st
        calDiscount += calProductSpecificDiscount
        discount.text = Utils.formatAmount(applicationContext, calDiscount, true)

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
        alertDialog = createLoader(this, "Creating order...")
        alertDialog.show()

        val c = getCacheStorage(applicationContext)

        val params = GuestCheckoutPlaceOrderParams(
            cartId = cart.id,
            billingAddress = AddressParams(
                street = c.get(Constants.streetLabel),
                streetTwo = Optional.presentIfNotNull(
                    c.get(Constants.street2Label),
                ),
                state = Optional.presentIfNotNull(
                    c.get(Constants.stateLabel)
                ),
                postcode = c.get(Constants.postcodeLabel),
                city = c.get(Constants.cityLabel),
                locationId = country.id,
                email = Optional.presentIfNotNull(c.get(Constants.emailLabel)),
                phone = Optional.presentIfNotNull(c.get(Constants.phoneLabel))
            ),
            shippingAddress = Optional.presentIfNotNull(
                AddressParams(
                    street = c.get(Constants.streetLabel),
                    streetTwo = Optional.presentIfNotNull(
                        c.get(Constants.street2Label),
                    ),
                    state = Optional.presentIfNotNull(
                        c.get(Constants.stateLabel)
                    ),
                    postcode = c.get(Constants.postcodeLabel),
                    city = c.get(Constants.cityLabel),
                    locationId = country.id,
                    email = Optional.presentIfNotNull(c.get(Constants.emailLabel)),
                    phone = Optional.presentIfNotNull(c.get(Constants.phoneLabel))
                )
            ),
            paymentMethodId = Optional.presentIfNotNull(paymentMethodV.id),
            shippingMethodId = Optional.presentIfNotNull(shippingMethodV.id),
            couponCode = Optional.presentIfNotNull(couponCode.text.toString()),
            firstName = c.get(Constants.firstNameLabel),
            lastName = c.get(Constants.lastNameLabel),
            email = c.get(Constants.emailLabel)
        )

        lifecycleScope.launch {
            presenter.placeOrder(applicationContext, params)
        }
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
        shippingFee.text = if (amount != 0) {
            Utils.formatAmount(applicationContext, amount, true)
        } else {
            "Free"
        }

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
        paymentFee.text = if (amount != 0) {
            Utils.formatAmount(applicationContext, amount, true)
        } else {
            "Free"
        }
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
        discount.text = Utils.formatAmount(applicationContext, amount, true)
        showMessage(applicationContext, "Coupon code applied")

        calculateOverview()
    }

    override fun onCheckDiscountFailure(err: ApiError) {
        showMessage(applicationContext, "Failed to apply coupon")
        calDiscount = 0
        discount.text = Utils.formatAmount(applicationContext, calDiscount, true)

        calculateOverview()
    }

    private fun calculateGrandTotal() {
        grandTotal.text = Utils.formatAmount(
            applicationContext,
            calSubtotal + calPaymentFee + calShippingFee + calProductSpecificDiscount - calDiscount,
            true
        )

        alertDialog.dismiss()
    }

    override fun onPlaceOrderSuccess(order: OrderGuestCheckoutMutation.OrderGuestCheckout) {
        showMessage(applicationContext, "Order created")

        val c = getCacheStorage(applicationContext)
        c.delete(Constants.cartIdLabel)
        c.delete(Constants.cartLabel)

        // TODO: Save to local order history (store specific)

        if (!paymentMethodV.isDigitalPayment) { // Handle Offline payment
            val i = Intent(applicationContext, OrderDetailsActivity::class.java)
            i.putExtra(Constants.orderHashLabel, order.hash)
            i.putExtra(Constants.orderCustomerEmailLabel, order.customer.email)
            startActivity(i)
            finish()
            return
        }

        // TODO: Handle Online payment
        initiateDigitalPayment(order)
    }

    override fun onPlaceOrderFailure(err: ApiError) {
        alertDialog.dismiss()
        showMessage(applicationContext, "Failed to create order")
    }

    override fun onBackPressed() {
        startActivity(Intent(applicationContext, CheckoutActivity::class.java))
        finish()
    }

    private fun initiateDigitalPayment(order: OrderGuestCheckoutMutation.OrderGuestCheckout) {
        this.order = order

        val i = Intent(applicationContext, DigitalPaymentActivity::class.java)
        i.putExtra(Constants.orderHashLabel, order.hash)
        i.putExtra(Constants.orderCustomerEmailLabel, order.customer.email)
        i.putExtra(Constants.orderIdLabel, order.id)
        startActivity(i)
        finish()
    }
}
