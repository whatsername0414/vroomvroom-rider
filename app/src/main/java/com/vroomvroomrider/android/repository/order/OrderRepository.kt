package com.vroomvroomrider.android.repository.order


import com.vroomvroomrider.android.data.model.order.Order
import com.vroomvroomrider.android.view.resource.Resource

interface OrderRepository {

    suspend fun getOrders(): Resource<List<Order>>?
    suspend fun getOrder(id: String): Resource<Order>?
    suspend fun pickOrder(id: String): Resource<Order>?
    suspend fun purchasedOrder(id: String): Resource<Order>?
    suspend fun arrived(id: String): Resource<Order>?
    suspend fun deliveredOrder(id: String): Resource<Order>?

}