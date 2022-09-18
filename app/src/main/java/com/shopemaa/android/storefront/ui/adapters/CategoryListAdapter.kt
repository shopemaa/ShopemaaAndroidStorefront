package com.shopemaa.android.storefront.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shopemaa.android.storefront.R
import com.shopemaa.android.storefront.api.graphql.CategoriesQuery
import com.shopemaa.android.storefront.ui.listeners.CategorySelectedListener

class CategoryListAdapter(
    var ctx: Context,
    private var categories: MutableList<CategoriesQuery.Category>,
    private var categorySelectedListener: CategorySelectedListener
) :
    RecyclerView.Adapter<CategoryListAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.category_list_item, parent, false)
        return CategoryViewHolder(view)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val c = categories[position]

        holder.categoryName.text = c.name
        holder.categoryName.setOnClickListener {
            categorySelectedListener.onSelected(c)
        }
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var categoryName: TextView = view.findViewById(R.id.category_name)
    }
}
