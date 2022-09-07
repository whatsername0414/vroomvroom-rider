package com.vroomvroomrider.android.view.ui.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ncorti.slidetoact.SlideToActView
import com.vroomvroomrider.android.R
import com.vroomvroomrider.android.data.model.order.Order
import com.vroomvroomrider.android.databinding.ItemOrderBinding

class OrderDiffUtil: DiffUtil.ItemCallback<Order>() {
    override fun areItemsTheSame(
        oldItem: Order,
        newItem: Order
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: Order,
        newItem: Order
    ): Boolean {
        return oldItem == newItem
    }

}

class OrderAdapter: ListAdapter<Order, OrderViewHolder>(OrderDiffUtil()) {

    var onOrderPicked: ((String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding: ItemOrderBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_order,
            parent,
            false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = getItem(position)
        holder.binding.apply {
            this.order = order
            commentGroup.isVisible = !order?.comment.isNullOrBlank()
            order?.orderDetail?.let { orderDetail ->
                totalOrderTv.text = root.context.getString(R.string.total_item,
                    orderDetail.totalItem)
                totalPriceTv.text = root.context.getString(R.string.total_price,
                    "%.2f".format(orderDetail.totalPrice))
            }
            holder.binding.sliderLayout.apply {
                root.visibility = View.VISIBLE
                pickOrderSlide.resetSlider()
                pickOrderSlide.onSlideCompleteListener = onSlideCompleteListener(order.id)

            }
        }
    }

    private fun onSlideCompleteListener(id: String): SlideToActView.OnSlideCompleteListener {
        return object : SlideToActView.OnSlideCompleteListener {
            override fun onSlideComplete(view: SlideToActView) {
                onOrderPicked?.invoke(id)
            }
        }
    }
}

class OrderViewHolder(val binding: ItemOrderBinding): RecyclerView.ViewHolder(binding.root)