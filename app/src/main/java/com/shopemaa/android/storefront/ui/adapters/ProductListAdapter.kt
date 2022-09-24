package com.shopemaa.android.storefront.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.shopemaa.android.storefront.R
import com.shopemaa.android.storefront.api.graphql.ProductsQuery
import com.shopemaa.android.storefront.ui.listeners.AddToCartListener
import com.shopemaa.android.storefront.utils.Utils
import java.net.URLDecoder

class ProductListAdapter(
    var ctx: Context,
    private var products: MutableList<ProductsQuery.ProductSearch>,
    private var addToCartListener: AddToCartListener
) :
    RecyclerView.Adapter<ProductListAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.product_list_item, parent, false)
        return ProductViewHolder(view)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val p = products[position]

        holder.productName.text = p.name
        holder.productPrice.text = Utils.formatAmount(ctx, p.price)

        if (p.fullImages.isNotEmpty()) {
            Glide.with(ctx).load(p.fullImages[0]).into(holder.productImage)
        }

        val des = URLDecoder.decode(p.description)
        holder.productDescription.text = des.substring(
            0, if (des.length > 50) {
                50
            } else {
                des.length
            }
        )

        if (p.productSpecificDiscount > 0) {
            holder.productStockOrDiscount.text =
                String.format("%d%% off", p.productSpecificDiscount)
            holder.productPrice.text =
                Utils.formatAmount(ctx, Utils.discountedPrice(p.productSpecificDiscount, p.price))
            holder.productDiscountedPrice.text = Utils.formatAmount(ctx, p.price)
            holder.productDiscountedPrice.paintFlags =
                holder.productDiscountedPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.productDiscountedPrice.visibility = View.VISIBLE
            holder.productStockOrDiscount.setTextColor(
                ContextCompat.getColor(
                    ctx,
                    R.color.primary
                )
            )
        } else {
            holder.productDiscountedPrice.visibility = View.GONE

            if (p.stock != null && p.stock > 0) {
                holder.productStockOrDiscount.text =
                    String.format("%d items left", p.stock)
                holder.productStockOrDiscount.setTextColor(
                    ContextCompat.getColor(
                        ctx,
                        R.color.secondary
                    )
                )
            } else {
                holder.productStockOrDiscount.text = "Out of stock"
                holder.productStockOrDiscount.setTextColor(
                    ContextCompat.getColor(
                        ctx,
                        R.color.highlighted
                    )
                )
            }
        }

        holder.addToCartBtn.setOnClickListener {
            if (p.stock == null || p.stock <= 0) {
                addToCartListener.onOutOfStock("${p.name} is out of stock")
                return@setOnClickListener
            }
            addToCartListener.onAdd(p.id)
        }
    }

    override fun getItemCount(): Int {
        return products.size
    }

    class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var productImage: ImageView = view.findViewById(R.id.product_image)
        var productName: TextView = view.findViewById(R.id.product_name)
        var productPrice: TextView = view.findViewById(R.id.product_price)
        var productStockOrDiscount: TextView =
            view.findViewById(R.id.product_stock_or_discount_amount)
        var productDiscountedPrice: TextView = view.findViewById(R.id.product_discounted_price)
        var productDescription: TextView = view.findViewById(R.id.product_description)
        var addToCartBtn: MaterialButton = view.findViewById(R.id.product_add_to_cart_btn)
    }
}
