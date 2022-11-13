package com.shopemaa.android.storefront.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import cn.pedant.SweetAlert.SweetAlertDialog
import com.arellomobile.mvp.presenter.InjectPresenter
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.shopemaa.android.storefront.R
import com.shopemaa.android.storefront.api.graphql.StoreBySecretQuery
import com.shopemaa.android.storefront.contants.Constants
import com.shopemaa.android.storefront.errors.ApiError
import com.shopemaa.android.storefront.storage.CacheStorage
import com.shopemaa.android.storefront.ui.presenters.StorePresenter
import com.shopemaa.android.storefront.ui.views.StoreView
import kotlinx.coroutines.launch

class StoreActivity : BaseActivity(), StoreView {
    private lateinit var storeLogo: ImageView
    private lateinit var storeName: TextView
    private lateinit var storeBrowseBtn: MaterialButton
    private lateinit var storeRescanBtn: Button

    private lateinit var alertDialog: SweetAlertDialog

    @InjectPresenter
    lateinit var presenter: StorePresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store)

        val c = CacheStorage(applicationContext)

        storeLogo = findViewById(R.id.store_logo)
        storeName = findViewById(R.id.store_name)
        storeBrowseBtn = findViewById(R.id.store_browse_btn)
        storeBrowseBtn.setOnClickListener {
            startActivity(Intent(applicationContext, HomeActivity::class.java))
            finish()
        }
        storeBrowseBtn.visibility = View.INVISIBLE
        storeRescanBtn = findViewById(R.id.store_rescan_btn)
        storeRescanBtn.setOnClickListener {
            c.cleanAll()
            startActivity(Intent(this, SplashActivity::class.java))
        }

        alertDialog = createLoader(this, "Loading...")

        lifecycleScope.launch {
            presenter.requestStore(applicationContext)
        }
    }

    override fun onStoreSuccess(store: StoreBySecretQuery.StoreBySecret) {
        alertDialog.dismiss()

        val c = CacheStorage(applicationContext)
        c.save(Constants.shopLabel, Gson().toJson(store))

        Glide.with(this).load(store.logo).into(storeLogo)

        storeName.text = store.name
        storeBrowseBtn.visibility = View.VISIBLE
    }

    override fun onStoreFailure(err: ApiError) {
        alertDialog.dismiss()
        startActivity(Intent(applicationContext, SplashActivity::class.java))
        finish()
    }

    override fun internetUnavailable() {
        showMessage(applicationContext, "No internet connection")
    }
}
