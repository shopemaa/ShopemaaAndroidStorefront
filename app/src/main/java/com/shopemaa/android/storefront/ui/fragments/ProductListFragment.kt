package com.shopemaa.android.storefront.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import cn.pedant.SweetAlert.SweetAlertDialog
import com.arellomobile.mvp.presenter.InjectPresenter
import com.shopemaa.android.storefront.R
import com.shopemaa.android.storefront.api.graphql.CartQuery
import com.shopemaa.android.storefront.api.graphql.CategoriesQuery
import com.shopemaa.android.storefront.api.graphql.ProductsQuery
import com.shopemaa.android.storefront.api.graphql.type.FilterKey
import com.shopemaa.android.storefront.api.graphql.type.FilterQuery
import com.shopemaa.android.storefront.contants.Constants
import com.shopemaa.android.storefront.errors.ApiError
import com.shopemaa.android.storefront.ui.adapters.ProductListAdapter
import com.shopemaa.android.storefront.ui.events.CartUpdateEvent
import com.shopemaa.android.storefront.ui.events.CategoryFilterEvent
import com.shopemaa.android.storefront.ui.events.ProductSearchEvent
import com.shopemaa.android.storefront.ui.listeners.AddToCartListener
import com.shopemaa.android.storefront.ui.presenters.CartPresenter
import com.shopemaa.android.storefront.ui.presenters.ProductListPresenter
import com.shopemaa.android.storefront.ui.views.CartView
import com.shopemaa.android.storefront.ui.views.ProductListView
import com.shopemaa.android.storefront.utils.CartUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ProductListFragment : BaseFragment(), ProductListView, CartView, AddToCartListener {
    private lateinit var productListView: RecyclerView
    private lateinit var productListAdapter: ProductListAdapter
    private lateinit var productList: MutableList<ProductsQuery.ProductSearch>

    private lateinit var swipeLayout: SwipeRefreshLayout

    private lateinit var productProgressBar: ProgressBar

    private var page: Int = 1
    private val perPage: Int = 20
    private var query: String? = null

    private var job: Job? = null

    @InjectPresenter
    lateinit var presenter: ProductListPresenter

    @InjectPresenter
    lateinit var cartPresenter: CartPresenter

    lateinit var alertDialog: SweetAlertDialog

    private var filterCategory: CategoriesQuery.Category? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.fragment_product_list, container, false)

        swipeLayout = v.findViewById(R.id.product_list_swiper)
        swipeLayout.setOnRefreshListener {
            swipeLayout.isRefreshing = false
            page = 1
            filterCategory = null
            this.productList.clear()
            onLoadProducts()
        }

        productProgressBar = v.findViewById(R.id.product_listing_loader)
        toggleProgressBar(false)

        productList = mutableListOf()
        productListAdapter = ProductListAdapter(requireContext(), productList, this)
        productListView = v.findViewById(R.id.product_list)
        val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        productListView.layoutManager = layoutManager
        productListView.adapter = productListAdapter

        onLoadProducts()

        productListView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (!recyclerView.canScrollVertically(1) && dy != 0) {
                    onLoadProducts()
                }
            }
        })

        EventBus.getDefault().register(this)

        return v
    }

    private fun onLoadProducts() {
        toggleProgressBar(true)

        if (job != null && job!!.isActive) {
            job!!.cancel()
        }

        val filters = mutableListOf<FilterQuery>()
        if (filterCategory != null) {
            filters.add(FilterQuery(FilterKey.category, filterCategory!!.id))
        }

        job = lifecycleScope.launch {
            if (query != null && query!!.isNotEmpty()) {
                presenter.requestProducts(requireContext(), query!!, page, perPage, filters)
            } else {
                presenter.requestProducts(requireContext(), page, perPage, filters)
            }
            page++
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onProducts(products: List<ProductsQuery.ProductSearch>) {
        activity?.runOnUiThread {
            this.productList.addAll(products)
            this.productListAdapter.notifyDataSetChanged()
        }
        toggleProgressBar(false)
    }

    override fun onProductsError(err: ApiError) {
        toggleProgressBar(false)
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onProductSearchChanged(event: ProductSearchEvent) {
        this.productList.clear()
        this.productListAdapter.notifyDataSetChanged()
        this.query = event.searchQuery
        this.page = 1

        onLoadProducts()
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onProductFilterByCategory(event: CategoryFilterEvent) {
        this.productList.clear()
        this.productListAdapter.notifyDataSetChanged()
        this.page = 1
        this.filterCategory = event.category

        onLoadProducts()
    }

    private fun toggleProgressBar(visible: Boolean) {
        activity?.runOnUiThread {
            if (visible) {
                productProgressBar.visibility = View.VISIBLE
            } else {
                productProgressBar.visibility = View.GONE
            }
        }
    }

    override fun onAdd(productId: String) {
        val c = getCacheStorage(requireContext())
        val cartId = c.get(Constants.cartIdLabel)

        alertDialog = createLoader(requireActivity(), "Processing...")
        alertDialog.show()

        if (cartId.isEmpty()) {
            Log.d("onAdd", "Cart is empty")
            lifecycleScope.launch {
                cartPresenter.requestCreateCart(requireContext(), productId)
            }
            return
        }

        Log.d("onAdd", "Cart is not empty")

        val cart = CartUtil.cartFromCache(c)
        lifecycleScope.launch {
            cartPresenter.requestUpdateCart(requireContext(), cart, productId)
        }
    }

    override fun onOutOfStock(msg: String) {
        if (::alertDialog.isInitialized) {
            alertDialog.dismiss()
        }
        showMessage(requireContext(), msg)
    }

    override fun onCartSuccess(cart: CartQuery.Cart) {
        val c = getCacheStorage(requireContext())
        c.save(Constants.cartIdLabel, cart.id)
        CartUtil.cartToCache(c, cart)
        alertDialog.dismiss()
        showMessage(requireContext(), "Added to cart")
        EventBus.getDefault().post(CartUpdateEvent())
    }

    override fun onCartFailure(err: ApiError) {
        alertDialog.dismiss()
        showMessage(requireContext(), "Add to Cart failed")
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}
