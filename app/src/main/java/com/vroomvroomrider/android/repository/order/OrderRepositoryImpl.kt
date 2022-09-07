package com.vroomvroomrider.android.repository.order

import android.util.Log
import com.vroomvroomrider.android.data.api.OrderService
import com.vroomvroomrider.android.data.model.order.Order
import com.vroomvroomrider.android.data.model.order.OrderMapper
import com.vroomvroomrider.android.data.model.order.StatusEnum
import com.vroomvroomrider.android.repository.base.BaseRepository
import com.vroomvroomrider.android.view.resource.Resource
import javax.inject.Inject

class OrderRepositoryImpl @Inject constructor(
    private val service: OrderService,
    private val mapper: OrderMapper
) : OrderRepository, BaseRepository() {

    override suspend fun getOrders(): Resource<List<Order>>? {
        var data: Resource<List<Order>>? = null
        try {
            val result = service.getOrders(
                ORDERS_QUERY_TYPE, StatusEnum.CONFIRMED.ordinal)
            result.body()?.data?.let {
                data = handleSuccess(mapper.mapToDomainModelList(it))
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error: ${e.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return data
    }

    override suspend fun getOrder(id: String): Resource<Order>? {
        var data: Resource<Order>? = null
        try {
            val result = service.getOrder(id)
            result.body()?.data?.let {
                data = handleSuccess(mapper.mapToDomainModel(it))
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error: ${e.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return data
    }

    override suspend fun pickOrder(id: String): Resource<Order>? {
        var data: Resource<Order>? = null
        try {
            val result = service.pickOrder(id)
            result.body()?.data?.let {
                data = handleSuccess(mapper.mapToDomainModel(it))
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error: ${e.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return data
    }

    override suspend fun purchasedOrder(id: String): Resource<Order>? {
        var data: Resource<Order>? = null
        try {
            val result = service.purchasedOrder(id)
            result.body()?.data?.let {
                data = handleSuccess(mapper.mapToDomainModel(it))
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error: ${e.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return data
    }

    override suspend fun arrived(id: String): Resource<Order>? {
        var data: Resource<Order>? = null
        try {
            val result = service.arrived(id)
            result.body()?.data?.let {
                data = handleSuccess(mapper.mapToDomainModel(it))
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error: ${e.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return data
    }

    override suspend fun deliveredOrder(id: String): Resource<Order>? {
        var data: Resource<Order>? = null
        try {
            val result = service.deliveredOrder(id)
            result.body()?.data?.let {
                data = handleSuccess(mapper.mapToDomainModel(it))
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error: ${e.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return data
    }

    companion object {
        const val TAG = "OrderRepositoryImpl"
        const val ORDERS_QUERY_TYPE = "rider"
    }
}