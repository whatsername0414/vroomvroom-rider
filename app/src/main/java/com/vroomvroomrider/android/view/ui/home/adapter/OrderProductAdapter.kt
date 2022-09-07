package com.vroomvroomrider.android.view.ui.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vroomvroomrider.android.R
import com.vroomvroomrider.android.data.model.order.OrderProduct
import com.vroomvroomrider.android.databinding.ItemOrderProductBinding
import com.vroomvroomrider.android.utils.Utils.getImageUrl

class OrderProductAdapter: ListAdapter<OrderProduct, OrderProductViewHolder>(OrdersDiffUtil()){

    var onProductClicked: ((String, direction: Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderProductViewHolder {
        val binding: ItemOrderProductBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_order_product,
            parent,
            false
        )
        return OrderProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderProductViewHolder, position: Int) {
        val orderProduct = getItem(position)
        holder.binding.apply {
            this.orderProduct = orderProduct
            orderProductPrice.text = holder.itemView.context.getString(
                R.string.peso, "%.2f".format(orderProduct.price))
            checkbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    onProductClicked?.invoke(orderProduct.id, 1)
                    Toast.makeText(
                        holder.itemView.context,
                        "Collected",
                        Toast.LENGTH_SHORT
                    ).show()
                    orderProductLayout.setBackgroundResource(
                        R.drawable.bg_red_f8e_rounded_16dp_stroke)
                    checkedLayout.visibility = View.VISIBLE
                } else {
                    onProductClicked?.invoke(orderProduct.id, 0)
                    orderProductLayout.background = null
                    checkedLayout.visibility = View.GONE
                }
            }
            Glide
                .with(holder.itemView.context)
                .load(getImageUrl(orderProduct.productImgUrl))
                .placeholder(R.drawable.ic_placeholder)
                .into(orderProductImage)
            val optionList = orderProduct.options?.map { "${it.optionType}: ${it.name}" }
            if (!optionList.isNullOrEmpty()) {
                holder.binding.orderProductOption.text = optionList.joinToString(", ")
            } else holder.binding.orderProductOption.visibility = View.GONE
        }
    }
}

class OrderProductViewHolder(val binding: ItemOrderProductBinding): RecyclerView.ViewHolder(binding.root)