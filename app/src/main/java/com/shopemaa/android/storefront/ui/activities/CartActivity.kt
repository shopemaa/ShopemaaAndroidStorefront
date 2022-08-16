package com.shopemaa.android.storefront.ui.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.arellomobile.mvp.presenter.InjectPresenter
import com.shopemaa.android.storefront.R
import com.shopemaa.android.storefront.api.graphql.CartQuery
import com.shopemaa.android.storefront.errors.ApiError
import com.shopemaa.android.storefront.ui.adapters.CartItemListAdapter
import com.shopemaa.android.storefront.ui.events.CartUpdateEvent
import com.shopemaa.android.storefront.ui.listeners.CartItemQuantityListener
import com.shopemaa.android.storefront.ui.presenters.CartPresenter
import com.shopemaa.android.storefront.ui.views.CartView
import com.shopemaa.android.storefront.utils.CartUtil
import com.shopemaa.android.storefront.utils.Utils
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

class CartActivity : BaseActivity(), CartItemQuantityListener, CartView {
    private lateinit var cartView: RecyclerView
    private lateinit var cartItems: MutableList<CartQuery.CartItem>
    private lateinit var cartAdapter: CartItemListAdapter

    private lateinit var cartViewBack: ImageView

    private lateinit var alertDialog: SweetAlertDialog

    private lateinit var itemCounter: TextView
    private lateinit var total: TextView

    @InjectPresenter
    lateinit var presenter: CartPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        itemCounter = findViewById(R.id.cart_total_items)
        total = findViewById(R.id.cart_total)

        cartViewBack = findViewById(R.id.cart_view_back)
        cartViewBack.setOnClickListener {
            finish()
        }

        cartView = findViewById(R.id.cart_items)
        val cart = CartUtil.cartFromCache(getCacheStorage(applicationContext))
        cartItems = mutableListOf()
        cartItems.addAll(cart.cartItems)
        cartAdapter = CartItemListAdapter(applicationContext, cartItems, this)
        val layoutManager = LinearLayoutManager(applicationContext, RecyclerView.VERTICAL, false)
        cartView.layoutManager = layoutManager
        cartView.adapter = cartAdapter

        updatePricingPart(cart)
    }

    override fun onChange(productId: String, qty: Int) {
        alertDialog = createLoader(this, "Updating...")
        alertDialog.show()

        val c = getCacheStorage(applicationContext)
        val cart = CartUtil.cartFromCache(c)

        lifecycleScope.launch {
            presenter.requestUpdateCart(applicationContext, cart, productId, qty)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCartSuccess(cart: CartQuery.Cart) {
        val c = getCacheStorage(applicationContext)
        CartUtil.cartToCache(c, cart)
        this.cartItems.clear()
        this.cartItems.addAll(cart.cartItems)
        cartAdapter.notifyDataSetChanged()
        alertDialog.dismiss()
        EventBus.getDefault().post(CartUpdateEvent())
        updatePricingPart(cart)
    }

    override fun onCartFailure(err: ApiError) {
        alertDialog.dismiss()
    }

    @SuppressLint("SetTextI18n")
    private fun updatePricingPart(cart: CartQuery.Cart) {
        var count = 0
        var total = 0
        cart.cartItems.forEach {
            count += it.quantity
            total += it.quantity * it.purchasePrice
        }

        itemCounter.text = "Items: $count"
        this.total.text = "Total: ${Utils.formatAmount(total, true)}"
    }

    override fun onBackPressed() {
        super.onBackPressed()

        finish()
    }
}