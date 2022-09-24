package com.shopemaa.android.storefront.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import cn.pedant.SweetAlert.SweetAlertDialog
import com.arellomobile.mvp.presenter.InjectPresenter
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.rengwuxian.materialedittext.MaterialEditText
import com.shopemaa.android.storefront.R
import com.shopemaa.android.storefront.api.graphql.CountriesQuery
import com.shopemaa.android.storefront.api.graphql.OrderGuestCheckoutMutation
import com.shopemaa.android.storefront.api.graphql.PaymentMethodsQuery
import com.shopemaa.android.storefront.api.graphql.ShippingMethodsQuery
import com.shopemaa.android.storefront.contants.Constants
import com.shopemaa.android.storefront.errors.ApiError
import com.shopemaa.android.storefront.models.PowerSpinnerModel
import com.shopemaa.android.storefront.ui.adapters.TwoFieldDropdownAdapter
import com.shopemaa.android.storefront.ui.presenters.CheckoutPresenter
import com.shopemaa.android.storefront.ui.views.CheckoutView
import com.shopemaa.android.storefront.utils.Utils
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener
import com.skydoves.powerspinner.PowerSpinnerView
import kotlinx.coroutines.launch

class CheckoutActivity : BaseActivity(), CheckoutView {
    private lateinit var firstName: MaterialEditText
    private lateinit var lastName: MaterialEditText
    private lateinit var email: MaterialEditText
    private lateinit var phone: MaterialEditText
    private lateinit var street: MaterialEditText
    private lateinit var street2: MaterialEditText
    private lateinit var state: MaterialEditText
    private lateinit var city: MaterialEditText
    private lateinit var postcode: MaterialEditText
    private lateinit var country: PowerSpinnerView
    private lateinit var paymentMethods: PowerSpinnerView
    private lateinit var shippingMethods: PowerSpinnerView
    private lateinit var continueBtn: MaterialButton

    private lateinit var countriesAdapter: TwoFieldDropdownAdapter
    private lateinit var paymentMethodsAdapter: TwoFieldDropdownAdapter
    private lateinit var shippingMethodsAdapter: TwoFieldDropdownAdapter

    private lateinit var backBtn: ImageView

    @InjectPresenter
    lateinit var presenter: CheckoutPresenter
    lateinit var alertDialog: SweetAlertDialog

    private var selectedCountry: CountriesQuery.Location? = null
    private var selectedShippingMethod: ShippingMethodsQuery.ShippingMethod? = null
    private var selectedPaymentMethod: PaymentMethodsQuery.PaymentMethod? = null

    private var fetchedCountries: MutableList<CountriesQuery.Location> = mutableListOf()
    private var fetchedShippingMethods: MutableList<ShippingMethodsQuery.ShippingMethod> =
        mutableListOf()
    private var fetchedPaymentMethods: MutableList<PaymentMethodsQuery.PaymentMethod> =
        mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        firstName = findViewById(R.id.checkout_first_name)
        lastName = findViewById(R.id.checkout_last_name)
        email = findViewById(R.id.checkout_email)
        phone = findViewById(R.id.checkout_phone)
        street = findViewById(R.id.checkout_street)
        street2 = findViewById(R.id.checkout_street2)
        state = findViewById(R.id.checkout_state)
        city = findViewById(R.id.checkout_city)
        postcode = findViewById(R.id.checkout_postcode)
        continueBtn = findViewById(R.id.checkout_continue_btn)
        continueBtn.setOnClickListener {
            onContinueToCompleteCheckout()
        }

        backBtn = findViewById(R.id.checkout_view_back)
        backBtn.setOnClickListener {
            onBackPressed()
        }

        alertDialog = createLoader(this, "Loading...")
        alertDialog.show()

        country = findViewById(R.id.checkout_country)
        countriesAdapter = TwoFieldDropdownAdapter(
            0,
            null,
            country
        )
        country.setSpinnerAdapter(countriesAdapter)
        country.spinnerPopupHeight = Utils.getScreenHeightDp(applicationContext) / 2
        countriesAdapter.setListener(object : TwoFieldDropdownAdapter.OnHolderItemSelectedListener {
            override fun onSelected(index: Int, item: PowerSpinnerModel, view: PowerSpinnerView) {
                selectedCountry = fetchedCountries[index]
                country.notifyItemSelected(index, item.title)
                country.dismiss()
            }
        })

        paymentMethods = findViewById(R.id.checkout_payment_method)
        paymentMethodsAdapter = TwoFieldDropdownAdapter(
            0,
            null,
            paymentMethods
        )
        paymentMethods.setSpinnerAdapter(paymentMethodsAdapter)
        paymentMethods.spinnerPopupHeight = Utils.getScreenHeightDp(applicationContext) / 2
        paymentMethodsAdapter.setListener(object :
            TwoFieldDropdownAdapter.OnHolderItemSelectedListener {
            override fun onSelected(index: Int, item: PowerSpinnerModel, view: PowerSpinnerView) {
                selectedPaymentMethod = fetchedPaymentMethods[index]
                paymentMethods.notifyItemSelected(index, item.title)
                paymentMethods.dismiss()
            }
        })

        shippingMethods = findViewById(R.id.checkout_shipping_method)
        shippingMethodsAdapter = TwoFieldDropdownAdapter(
            0,
            null,
            shippingMethods
        )
        shippingMethods.setSpinnerAdapter(shippingMethodsAdapter)
        shippingMethods.spinnerPopupHeight = Utils.getScreenHeightDp(applicationContext) / 2
        shippingMethodsAdapter.setListener(object :
            TwoFieldDropdownAdapter.OnHolderItemSelectedListener {
            override fun onSelected(index: Int, item: PowerSpinnerModel, view: PowerSpinnerView) {
                selectedShippingMethod = fetchedShippingMethods[index]
                shippingMethods.notifyItemSelected(index, item.title)
                shippingMethods.dismiss()
            }
        })

        lifecycleScope.launch {
            presenter.requestCountries(applicationContext)
        }
    }

    private fun onContinueToCompleteCheckout() {
        if (isFieldsValid()) {
            startActivity(Intent(applicationContext, CheckoutCompleteActivity::class.java))
            finish()
        }
    }

    private fun isFieldsValid(): Boolean {
        val c = getCacheStorage(applicationContext)

        if (firstName.text?.trim()!!.isEmpty()) {
            showMessage(applicationContext, "First name is required")
            return false
        }
        if (lastName.text?.trim()!!.isEmpty()) {
            showMessage(applicationContext, "Last name is required")
            return false
        }
        if (email.text?.trim()!!.isEmpty()) {
            showMessage(applicationContext, "Email is required")
            return false
        }
        if (phone.text?.trim()!!.isEmpty()) {
            showMessage(applicationContext, "Phone is required")
            return false
        }
        if (street.text?.trim()!!.isEmpty()) {
            showMessage(applicationContext, "Street is required")
            return false
        }
        if (postcode.text?.trim()!!.isEmpty()) {
            showMessage(applicationContext, "Postcode is required")
            return false
        }
        if (city.text?.trim()!!.isEmpty()) {
            showMessage(applicationContext, "City is required")
            return false
        }
        if (selectedCountry == null) {
            showMessage(applicationContext, "Country is required")
            return false
        }
        if (selectedPaymentMethod == null) {
            showMessage(applicationContext, "Payment method is required")
            return false
        }
        if (selectedShippingMethod == null) {
            showMessage(applicationContext, "Shipping method is required")
            return false
        }

        c.save(Constants.firstNameLabel, firstName.text?.trim().toString())
        c.save(Constants.lastNameLabel, lastName.text?.trim().toString())
        c.save(Constants.emailLabel, email.text?.trim().toString())
        c.save(Constants.phoneLabel, phone.text?.trim().toString())
        c.save(Constants.streetLabel, street.text?.trim().toString())
        c.save(Constants.street2Label, street2.text?.trim().toString())
        c.save(Constants.stateLabel, state.text?.trim().toString())
        c.save(Constants.postcodeLabel, postcode.text?.trim().toString())
        c.save(Constants.cityLabel, city.text?.trim().toString())
        c.save(Constants.countryLabel, Gson().toJson(selectedCountry))
        c.save(Constants.shippingMethodLabel, Gson().toJson(selectedShippingMethod))
        c.save(Constants.paymentMethodLabel, Gson().toJson(selectedPaymentMethod))

        return true
    }

    override fun onCountryListSuccess(countries: List<CountriesQuery.Location>) {
        val items = countries.map {
            PowerSpinnerModel(it.id, it.name, it.shortCode)
        }
        countriesAdapter.setItems(items)
        fetchedCountries.addAll(countries)

        lifecycleScope.launch {
            presenter.requestPaymentMethods(applicationContext)
        }
    }

    override fun onCountryListFailure(err: ApiError) {
        lifecycleScope.launch {
            presenter.requestCountries(applicationContext)
        }
    }

    override fun onPaymentMethodListSuccess(methods: List<PaymentMethodsQuery.PaymentMethod>) {
        val items = methods.map {
            PowerSpinnerModel(
                it.id,
                it.displayName,
                "${it.currencyName} - ${
                    if (it.isDigitalPayment) {
                        "Online Payment"
                    } else {
                        "Cash Payment"
                    }
                }"
            )
        }
        paymentMethodsAdapter.setItems(items)
        fetchedPaymentMethods.addAll(methods)

        lifecycleScope.launch {
            presenter.requestShippingMethods(applicationContext)
        }
    }

    override fun onPaymentMethodListFailure(err: ApiError) {
        lifecycleScope.launch {
            presenter.requestPaymentMethods(applicationContext)
        }
    }

    override fun onShippingMethodListSuccess(methods: List<ShippingMethodsQuery.ShippingMethod>) {
        alertDialog.dismiss()

        val items = methods.map {
            PowerSpinnerModel(
                it.id,
                it.displayName,
                "${
                    Utils.formatAmount(
                        applicationContext,
                        it.deliveryCharge
                    )
                } - Approx. delivery in ${it.deliveryTimeInDays} days"
            )
        }
        shippingMethodsAdapter.setItems(items)
        fetchedShippingMethods.addAll(methods)
    }

    override fun onShippingMethodListFailure(err: ApiError) {
        lifecycleScope.launch {
            presenter.requestShippingMethods(applicationContext)
        }
    }

    override fun onCheckShippingFeeSuccess(amount: Int) {

    }

    override fun onCheckShippingFeeFailure(err: ApiError) {

    }

    override fun onCheckPaymentFeeSuccess(amount: Int) {

    }

    override fun onCheckPaymentFeeFailure(err: ApiError) {

    }

    override fun onCheckDiscountSuccess(amount: Int) {

    }

    override fun onCheckDiscountFailure(err: ApiError) {

    }

    override fun onPlaceOrderSuccess(order: OrderGuestCheckoutMutation.OrderGuestCheckout) {

    }

    override fun onPlaceOrderFailure(err: ApiError) {

    }

    override fun onBackPressed() {
        startActivity(Intent(applicationContext, CartActivity::class.java))
        finish()
    }
}
