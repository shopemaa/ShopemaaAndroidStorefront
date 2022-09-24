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
import com.shopemaa.android.storefront.api.graphql.OrderByCustomerEmailQuery
import com.shopemaa.android.storefront.utils.Utils

class OrderItemListAdapter(
    var ctx: Context,
    private var cartItems: MutableList<OrderByCustomerEmailQuery.CartItem>,
) : RecyclerView.Adapter<OrderItemListAdapter.OrderItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.order_item, parent, false)
        return OrderItemViewHolder(view)
    }

    @SuppressLint("ResourceAsColor", "SetTextI18n")
    override fun onBindViewHolder(holder: OrderItemViewHolder, position: Int) {
        val item = cartItems[position]

        holder.productName.text = item.product.name

        if (item.product.fullImages.isNotEmpty()) {
            Glide.with(ctx).load(item.product.fullImages[0]).into(holder.productImage)
        }

        holder.productPriceWithQuantity.text =
            "${Utils.formatAmount(ctx, item.purchasePrice, true)} x ${item.quantity}"
        holder.productTotal.text = Utils.formatAmount(ctx, item.purchasePrice * item.quantity, true)

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

    class OrderItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var productImage: ImageView = view.findViewById(R.id.product_image)
        var productName: TextView = view.findViewById(R.id.product_name)
        var productPriceWithQuantity: TextView = view.findViewById(R.id.product_price_with_quantity)
        var productTotal: TextView = view.findViewById(R.id.product_total)
        var attributes: TextView = view.findViewById(R.id.attributes)
    }
}
