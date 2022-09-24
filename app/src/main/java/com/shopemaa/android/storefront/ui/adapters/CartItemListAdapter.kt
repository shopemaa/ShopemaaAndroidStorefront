package com.shopemaa.android.storefront.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.shopemaa.android.storefront.R
import com.shopemaa.android.storefront.api.graphql.CartQuery
import com.shopemaa.android.storefront.ui.listeners.CartItemQuantityListener
import com.shopemaa.android.storefront.utils.Utils

class CartItemListAdapter(
    var ctx: Context,
    private var cartItems: MutableList<CartQuery.CartItem>,
    private var qtyChangeListener: CartItemQuantityListener
) : RecyclerView.Adapter<CartItemListAdapter.CartItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cart_item, parent, false)
        return CartItemViewHolder(view)
    }

    @SuppressLint("ResourceAsColor", "SetTextI18n")
    override fun onBindViewHolder(holder: CartItemViewHolder, position: Int) {
        val item = cartItems[position]

        holder.productName.text = item.product.name
        holder.productPrice.text = Utils.formatAmount(ctx, item.purchasePrice)

        if (item.product.fullImages.isNotEmpty()) {
            Glide.with(ctx).load(item.product.fullImages[0]).into(holder.productImage)
        }

        val price = if (item.product.productSpecificDiscount > 0) {
            Utils.discountedPrice(item.product.productSpecificDiscount, item.purchasePrice)
        } else {
            item.purchasePrice
        }

        holder.productPrice.text =
            Utils.formatAmount(ctx, price, true)
        holder.productQty.text = item.quantity.toString()
        holder.productQtyDown.setOnClickListener {
            qtyChangeListener.onChange(item.product.id, item.quantity - 1)
        }
        holder.productQtyUp.setOnClickListener {
            if (item.product.stock != null && item.quantity < item.product.stock) {
                qtyChangeListener.onChange(item.product.id, item.quantity + 1)
            }
        }
        holder.productStock.text = "${item.product.stock} left"

        if (item.attributes.isEmpty()) {
            holder.attributes.visibility = View.GONE
        } else {
            holder.attributes.visibility = View.VISIBLE
            var t = ""
            var isFirst = false
            item.attributes.forEach { at ->
                if (isFirst) {
                    t += ", "
                }
                t += "${at.name}: ${at.selectedValue}"
                isFirst = true
            }

            holder.attributes.text = t
        }

    }

    override fun getItemCount(): Int {
        return cartItems.size
    }

    class CartItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var productImage: ImageView = view.findViewById(R.id.product_image)
        var productName: TextView = view.findViewById(R.id.product_name)
        var productPrice: TextView = view.findViewById(R.id.product_price)
        var productQtyUp: ImageView = view.findViewById(R.id.product_qty_up)
        var productQty: TextView = view.findViewById(R.id.product_qty)
        var productQtyDown: ImageView = view.findViewById(R.id.product_qty_down)
        var productStock: TextView = view.findViewById(R.id.product_stock)
        var attributes: TextView = view.findViewById(R.id.attributes)
    }
}
