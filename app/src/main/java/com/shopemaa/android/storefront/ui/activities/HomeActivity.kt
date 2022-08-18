package com.shopemaa.android.storefront.ui.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.arellomobile.mvp.presenter.InjectPresenter
import com.shopemaa.android.storefront.R
import com.shopemaa.android.storefront.api.graphql.CategoriesQuery
import com.shopemaa.android.storefront.contants.Constants
import com.shopemaa.android.storefront.errors.ApiError
import com.shopemaa.android.storefront.models.PowerSpinnerModel
import com.shopemaa.android.storefront.ui.adapters.TwoFieldDropdownAdapter
import com.shopemaa.android.storefront.ui.events.CartUpdateEvent
import com.shopemaa.android.storefront.ui.events.CategoryFilterEvent
import com.shopemaa.android.storefront.ui.events.ProductSearchEvent
import com.shopemaa.android.storefront.ui.fragments.CustomerProfileFragment
import com.shopemaa.android.storefront.ui.fragments.OrderListFragment
import com.shopemaa.android.storefront.ui.fragments.ProductListFragment
import com.shopemaa.android.storefront.ui.presenters.CategoryPresenter
import com.shopemaa.android.storefront.ui.views.CategoryView
import com.shopemaa.android.storefront.utils.CartUtil
import com.skydoves.powerspinner.PowerSpinnerView
import github.com.st235.lib_expandablebottombar.ExpandableBottomBar
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class HomeActivity : BaseActivity(), TextWatcher, CategoryView {
    private lateinit var homeFragmentViewer: RelativeLayout
    private lateinit var inputSearch: EditText

    private lateinit var expandableBottomBar: ExpandableBottomBar

    private lateinit var homeMenu: ImageView
    private lateinit var cartCounter: TextView
    private lateinit var cartIcon: ImageView

    private var selectedIndex = Constants.indexHome

    private lateinit var selectedCategory: CategoriesQuery.Category
    private lateinit var categories: List<CategoriesQuery.Category>

    @InjectPresenter
    lateinit var presenter: CategoryPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        EventBus.getDefault().register(this)

        disableAutoFocus()

        homeMenu = findViewById(R.id.cart_view_back)
        homeFragmentViewer = findViewById(R.id.shop_tab_viewer)
        inputSearch = findViewById(R.id.input_search)
        expandableBottomBar = findViewById(R.id.shop_home_expandable_bottom_bar)
        cartCounter = findViewById(R.id.cart_item_counter)
        cartIcon = findViewById(R.id.product_search_cart)

        toggleMainView(Constants.indexHome)

        inputSearch.addTextChangedListener(this)

        expandableBottomBar.onItemSelectedListener = { _, menuItem, _ ->
            when (menuItem.text.toString().lowercase()) {
                "home" -> {
                    toggleMainView(Constants.indexHome)
                }
                "orders" -> {
                    toggleMainView(Constants.indexOrder)
                }
                "profile" -> {
                    toggleMainView(Constants.indexProfile)
                }
            }
        }

        homeMenu.setOnClickListener {
            if (selectedIndex == Constants.indexHome) {
                showCategories()
            }
        }
        cartIcon.setOnClickListener {
            startActivity(Intent(applicationContext, CartActivity::class.java))
            finish()
        }

        lifecycleScope.launch {
            presenter.requestCategories(applicationContext, null, 1, 1000)
        }

        updateCartCounter()
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        when (selectedIndex) {
            Constants.indexHome -> {
                EventBus.getDefault().post(ProductSearchEvent(inputSearch.text.toString()))
            }
            Constants.indexOrder -> {
//                EventBus.getDefault().post(OrderSearchEvent(inputSearch.text.toString()))
            }
        }
    }

    override fun afterTextChanged(p0: Editable?) {

    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    private fun toggleMainView(index: Int) {
        selectedIndex = index

        val tx = supportFragmentManager.beginTransaction()
        when (index) {
            Constants.indexHome -> {
                tx.replace(R.id.shop_tab_viewer, ProductListFragment()).commitNow()
                inputSearch.hint = "Search Products"
                inputSearch.visibility = View.VISIBLE
            }
            Constants.indexOrder -> {
                tx.replace(R.id.shop_tab_viewer, OrderListFragment()).commitNow()
                inputSearch.hint = "Search Orders by Hash"
                inputSearch.visibility = View.INVISIBLE
            }
            Constants.indexProfile -> {
                tx.replace(R.id.shop_tab_viewer, CustomerProfileFragment()).commitNow()
                inputSearch.visibility = View.INVISIBLE
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onCartUpdated(event: CartUpdateEvent) {
        updateCartCounter()
    }

    private fun updateCartCounter() {
        var cartItems = 0
        val cartId = cacheStorage.get(Constants.cartIdLabel)
        if (cartId.isNotEmpty()) {
            val cart = CartUtil.cartFromCache(cacheStorage)
            cart.cartItems.forEach {
                cartItems += it.quantity
            }
        }

        runOnUiThread {
            cartCounter.text = cartItems.toString()
        }
    }

    private fun showCategories() {
        if (::categories.isInitialized.not()) {
            return
        }

        val v =
            LayoutInflater.from(applicationContext).inflate(R.layout.category_selection_view, null)
        val categoriesPowerMenu = v.findViewById<PowerSpinnerView>(R.id.home_categories)
        val categoriesAdapter = TwoFieldDropdownAdapter(
            0,
            null,
            categoriesPowerMenu
        )
        categoriesPowerMenu.setSpinnerAdapter(categoriesAdapter)
        categoriesPowerMenu.spinnerPopupHeight = 700
        categoriesAdapter.setListener(object :
            TwoFieldDropdownAdapter.OnHolderItemSelectedListener {
            override fun onSelected(index: Int, item: PowerSpinnerModel) {
                selectedCategory = categories[index]
                categoriesPowerMenu.notifyItemSelected(index, item.title)
                categoriesPowerMenu.dismiss()
            }
        })
        categoriesAdapter.setItems(categories.map {
            PowerSpinnerModel(it.id, it.name, "")
        })

        if (::selectedCategory.isInitialized) {
            val index = categories.indexOf(selectedCategory)
            if (index < 0) {
                return
            }
            categoriesPowerMenu.notifyItemSelected(index, categories[index].name)
        }

        val alert = createCustomPopup(
            this, v
        ) {
            if (::selectedCategory.isInitialized) {
                EventBus.getDefault().post(CategoryFilterEvent(selectedCategory))
            }
            it.dismiss()
        }
        alert.show()
    }

    override fun onCategoriesSuccess(categories: List<CategoriesQuery.Category>) {
        this.categories = categories
    }

    override fun onCategoriesFailure(err: ApiError) {

    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}
