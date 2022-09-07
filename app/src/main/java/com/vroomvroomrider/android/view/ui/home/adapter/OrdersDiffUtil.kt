package com.vroomvroomrider.android.view.ui.home.adapter

import androidx.recyclerview.widget.DiffUtil
import com.vroomvroomrider.android.data.model.order.OrderProduct

class OrdersDiffUtil: DiffUtil.ItemCallback<OrderProduct>() {
    override fun areItemsTheSame(
        oldItem: OrderProduct,
        newItem: OrderProduct
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: OrderProduct,
        newItem: OrderProduct
    ): Boolean {
        return oldItem == newItem
    }
}