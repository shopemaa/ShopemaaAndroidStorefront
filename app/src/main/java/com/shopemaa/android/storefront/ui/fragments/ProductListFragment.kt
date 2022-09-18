package com.shopemaa.android.storefront.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
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
import com.shopemaa.android.storefront.models.PowerSpinnerModel
import com.shopemaa.android.storefront.ui.adapters.CategoryListAdapter
import com.shopemaa.android.storefront.ui.adapters.ProductListAdapter
import com.shopemaa.android.storefront.ui.adapters.TwoFieldDropdownAdapter
import com.shopemaa.android.storefront.ui.events.CartUpdateEvent
import com.shopemaa.android.storefront.ui.events.CategoryFilterEvent
import com.shopemaa.android.storefront.ui.events.ProductSearchEvent
import com.shopemaa.android.storefront.ui.listeners.AddToCartListener
import com.shopemaa.android.storefront.ui.listeners.CategorySelectedListener
import com.shopemaa.android.storefront.ui.presenters.CartPresenter
import com.shopemaa.android.storefront.ui.presenters.CategoryPresenter
import com.shopemaa.android.storefront.ui.presenters.ProductListPresenter
import com.shopemaa.android.storefront.ui.views.CartView
import com.shopemaa.android.storefront.ui.views.CategoryView
import com.shopemaa.android.storefront.ui.views.ProductListView
import com.shopemaa.android.storefront.utils.CartUtil
import com.skydoves.powerspinner.PowerSpinnerView
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ProductListFragment : BaseFragment(), ProductListView, CartView, CategoryView,
    AddToCartListener,
    CategorySelectedListener {
    private lateinit var productListView: RecyclerView
    private lateinit var productListAdapter: ProductListAdapter
    private lateinit var productList: MutableList<ProductsQuery.ProductSearch>

    private lateinit var categoryListView: RecyclerView
    private lateinit var categoryListAdapter: CategoryListAdapter
    private lateinit var categoryList: MutableList<CategoriesQuery.Category>

    private lateinit var swipeLayout: SwipeRefreshLayout

    private lateinit var productProgressBar: ProgressBar

    private var page: Int = 1
    private val perPage: Int = 20
    private var query: String? = null

    private var job: Job? = null

    @InjectPresenter
    lateinit var presenter: ProductListPresenter

    @InjectPresenter
    lateinit var categoryPresenter: CategoryPresenter

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

        categoryList = mutableListOf()
        categoryListAdapter = CategoryListAdapter(requireContext(), categoryList, this)
        categoryListView = v.findViewById(R.id.category_list)
        val categoryLayoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        categoryListView.layoutManager = categoryLayoutManager
        categoryListView.adapter = categoryListAdapter

        productList = mutableListOf()
        productListAdapter = ProductListAdapter(requireContext(), productList, this)
        productListView = v.findViewById(R.id.product_list)
        val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        productListView.layoutManager = layoutManager
        productListView.adapter = productListAdapter

        lifecycleScope.launch {
            categoryPresenter.requestCategories(requireContext(), null, 1, 100)
        }

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
    override fun onCategoriesSuccess(categories: List<CategoriesQuery.Category>) {
        activity?.runOnUiThread {
            this.categoryList.add(CategoriesQuery.Category("All", "All", "all", "", "", "", 0, 0))
            this.categoryList.addAll(categories)
            this.categoryListAdapter.notifyDataSetChanged()
        }
    }

    override fun onCategoriesFailure(err: ApiError) {
        showMessage(requireContext(), "Category listing failed")
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
        val prod = productList.find { it.id == productId }
        val selectedAttributes: MutableMap<String, String> = mutableMapOf()

        if (prod?.attributes.isNullOrEmpty().not()) {
            val layout = LinearLayout(requireContext())
            layout.orientation = LinearLayout.VERTICAL
            layout.layoutParams = ViewGroup.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )

            prod?.attributes?.forEach { at ->
                layout.addView(
                    createSpinner(requireContext(), "${at.name}${
                        if (at.isRequired) {
                            "*"
                        } else {
                            ""
                        }
                    }",
                        object : TwoFieldDropdownAdapter.OnHolderItemSelectedListener {
                            override fun onSelected(
                                index: Int,
                                item: PowerSpinnerModel,
                                view: PowerSpinnerView
                            ) {
                                selectedAttributes[item.id] = item.title
                                view.notifyItemSelected(index, item.title)
                                view.dismiss()
                            }
                        }, at.values.map { PowerSpinnerModel(at.id, it, "") })
                )
            }

            val alert = createCustomPopup(
                requireActivity(),
                "Please select attributes",
                layout, "Done", false,
            ) {
                if (prod?.attributes?.any { at -> at.isRequired && selectedAttributes[at.id] == null } == false) {
                    it.dismiss()
                    createOrUpdateCart(productId, selectedAttributes)
                }
            }
            alert.show()
        } else {
            createOrUpdateCart(productId, selectedAttributes)
        }
    }

    private fun createOrUpdateCart(productId: String, attributes: MutableMap<String, String>) {
        val c = getCacheStorage(requireContext())
        val cartId = c.get(Constants.cartIdLabel)

        alertDialog = createLoader(requireActivity(), "Processing...")
        alertDialog.show()

        if (cartId.isEmpty()) {
            lifecycleScope.launch {
                cartPresenter.requestCreateCart(requireContext(), productId, attributes)
            }
            return
        }

        val cart = CartUtil.cartFromCache(c)
        lifecycleScope.launch {
            cartPresenter.requestUpdateCart(requireContext(), cart, productId, attributes)
        }
    }

    override fun onOutOfStock(msg: String) {
        if (::alertDialog.isInitialized) {
            alertDialog.dismiss()
        }
        showMessage(requireContext(), msg)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onSelected(c: CategoriesQuery.Category) {
        this.productList.clear()
        this.productListAdapter.notifyDataSetChanged()
        this.page = 1
        this.filterCategory = if (c.id.equals("All")) {
            null
        } else {
            c
        }

        onLoadProducts()
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
