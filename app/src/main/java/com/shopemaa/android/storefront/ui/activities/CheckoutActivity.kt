package com.shopemaa.android.storefront.ui.activities

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import cn.pedant.SweetAlert.SweetAlertDialog
import com.arellomobile.mvp.presenter.InjectPresenter
import com.google.android.material.button.MaterialButton
import com.rengwuxian.materialedittext.MaterialEditText
import com.shopemaa.android.storefront.R
import com.shopemaa.android.storefront.api.graphql.CountriesQuery
import com.shopemaa.android.storefront.api.graphql.PaymentMethodsQuery
import com.shopemaa.android.storefront.api.graphql.ShippingMethodsQuery
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
    private lateinit var city: MaterialEditText
    private lateinit var postcode: MaterialEditText
    private lateinit var country: PowerSpinnerView
    private lateinit var paymentMethods: PowerSpinnerView
    private lateinit var shippingMethods: PowerSpinnerView
    private lateinit var continueBtn: MaterialButton

    private lateinit var countriesAdapter: TwoFieldDropdownAdapter
    private lateinit var paymentMethodsAdapter: TwoFieldDropdownAdapter
    private lateinit var shippingMethodsAdapter: TwoFieldDropdownAdapter

    @InjectPresenter
    lateinit var presenter: CheckoutPresenter
    lateinit var alertDialog: SweetAlertDialog

    private var selectedCountry: String? = null
    private var selectedShippingMethod: String? = null
    private var selectedPaymentMethod: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        firstName = findViewById(R.id.checkout_first_name)
        lastName = findViewById(R.id.checkout_last_name)
        email = findViewById(R.id.checkout_email)
        phone = findViewById(R.id.checkout_phone)
        street = findViewById(R.id.checkout_street)
        street2 = findViewById(R.id.checkout_street2)
        city = findViewById(R.id.checkout_city)
        postcode = findViewById(R.id.checkout_postcode)
        continueBtn = findViewById(R.id.checkout_continue_btn)
        continueBtn.setOnClickListener {
            onContinueToCompleteCheckout()
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
        country.spinnerPopupHeight = 500
        countriesAdapter.setListener(object : TwoFieldDropdownAdapter.OnHolderItemSelectedListener {
            override fun onSelected(index: Int, item: PowerSpinnerModel) {
                selectedCountry = item.id
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
        paymentMethods.spinnerPopupHeight = 500
        paymentMethodsAdapter.setListener(object :
            TwoFieldDropdownAdapter.OnHolderItemSelectedListener {
            override fun onSelected(index: Int, item: PowerSpinnerModel) {
                selectedPaymentMethod = item.id
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
        shippingMethods.spinnerPopupHeight = 500
        shippingMethodsAdapter.setListener(object :
            TwoFieldDropdownAdapter.OnHolderItemSelectedListener {
            override fun onSelected(index: Int, item: PowerSpinnerModel) {
                selectedShippingMethod = item.id
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

        }
    }

    private fun isFieldsValid(): Boolean {

        return true
    }

    override fun onCountryListSuccess(countries: List<CountriesQuery.Location>) {
        val items = countries.map {
            PowerSpinnerModel(it.id, it.name, it.shortCode)
        }
        countriesAdapter.setItems(items)

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
                        "Cash on Delivery"
                    }
                }"
            )
        }
        paymentMethodsAdapter.setItems(items)

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
                "${Utils.formatAmount(it.deliveryCharge)} - Approx. delivery in ${it.deliveryTimeInDays} days"
            )
        }
        shippingMethodsAdapter.setItems(items)
    }

    override fun onShippingMethodListFailure(err: ApiError) {
        lifecycleScope.launch {
            presenter.requestShippingMethods(applicationContext)
        }
    }

}
