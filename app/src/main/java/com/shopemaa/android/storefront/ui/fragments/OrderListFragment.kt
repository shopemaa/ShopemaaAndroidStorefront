package com.shopemaa.android.storefront.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.button.MaterialButton
import com.rengwuxian.materialedittext.MaterialEditText
import com.shopemaa.android.storefront.R
import com.shopemaa.android.storefront.contants.Constants
import com.shopemaa.android.storefront.ui.activities.OrderDetailsActivity

class OrderListFragment : BaseFragment() {
    private lateinit var orderHash: MaterialEditText
    private lateinit var customerEmail: MaterialEditText
    private lateinit var orderDetailsBtn: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_order_list, container, false)

        orderHash = v.findViewById(R.id.order_hash)
        customerEmail = v.findViewById(R.id.customer_email)
        orderDetailsBtn = v.findViewById(R.id.order_details_btn)
        orderDetailsBtn.setOnClickListener {
            if (orderHash.text.toString().isNotEmpty() && customerEmail.text.toString()
                    .isNotEmpty()
            ) {
                val i = Intent(requireContext(), OrderDetailsActivity::class.java)
                i.putExtra(Constants.orderHashLabel, orderHash.text.toString())
                i.putExtra(Constants.orderCustomerEmailLabel, customerEmail.text.toString())
                startActivity(i)
            }
        }

        return v
    }
}
