package com.shopemaa.android.storefront.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shopemaa.android.storefront.R
import com.shopemaa.android.storefront.models.PowerSpinnerModel
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener
import com.skydoves.powerspinner.PowerSpinnerInterface
import com.skydoves.powerspinner.PowerSpinnerView

class TwoFieldDropdownAdapter(
    override var index: Int,
    override var onSpinnerItemSelectedListener: OnSpinnerItemSelectedListener<PowerSpinnerModel>?,
    override val spinnerView: PowerSpinnerView
) : PowerSpinnerInterface<PowerSpinnerModel>,
    RecyclerView.Adapter<TwoFieldDropdownAdapter.TwoFieldDropdownViewHolder>() {

    private var items: MutableList<PowerSpinnerModel> = mutableListOf()
    private var listener: OnHolderItemSelectedListener? = null

    override fun getItemCount(): Int {
        return items.size
    }

    override fun notifyItemSelected(index: Int) {

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun setItems(itemList: List<PowerSpinnerModel>) {
        items.addAll(itemList)
        notifyDataSetChanged()
    }

    fun setListener(listener: OnHolderItemSelectedListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TwoFieldDropdownViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.two_field_dropdown_list_item, parent, false)
        return TwoFieldDropdownViewHolder(view, spinnerView)
    }

    override fun onBindViewHolder(holder: TwoFieldDropdownViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.subtitle.text = item.subtitle
        holder.item = item
        holder.itemIndex = position
        holder.clickListener = listener
    }

    fun getItem(index: Int): PowerSpinnerModel {
        return items[index]
    }

    class TwoFieldDropdownViewHolder(v: View, spinnerView: PowerSpinnerView) :
        RecyclerView.ViewHolder(v) {
        var title: TextView = v.findViewById(R.id.two_field_title)
        var subtitle: TextView = v.findViewById(R.id.two_field_subtitle)
        var clickListener: OnHolderItemSelectedListener? = null
        var itemIndex: Int = 0
        var item: PowerSpinnerModel? = null

        init {
            v.setOnClickListener {
                clickListener?.onSelected(itemIndex, item!!, spinnerView)
            }
        }
    }

    interface OnHolderItemSelectedListener {
        fun onSelected(index: Int, item: PowerSpinnerModel, view: PowerSpinnerView)
    }
}
