package com.vroomvroomrider.android.data.api

import com.vroomvroomrider.android.data.model.BaseResponse
import com.vroomvroomrider.android.data.model.BaseResponseList
import com.vroomvroomrider.android.data.model.order.OrderDto
import retrofit2.Response
import retrofit2.http.*

interface OrderService {

    @GET("orders")
    suspend fun getOrders(
        @Query("type") type: String,
        @Query("status") status: Int
    ): Response<BaseResponseList<OrderDto>>

    @GET("orders/{id}")
    suspend fun getOrder(
        @Path("id") id: String
    ): Response<BaseResponse<OrderDto>>

    @PATCH("orders/{id}/pick")
    suspend fun pickOrder(
        @Path("id") id: String
    ): Response<BaseResponse<OrderDto>>

    @PATCH("orders/{id}/purchased")
    suspend fun purchasedOrder(
        @Path("id") id: String
    ): Response<BaseResponse<OrderDto>>

    @PATCH("orders/{id}/arrived")
    suspend fun arrived(
        @Path("id") id: String
    ): Response<BaseResponse<OrderDto>>

    @PATCH("orders/{id}/delivered")
    suspend fun deliveredOrder(
        @Path("id") id: String
    ): Response<BaseResponse<OrderDto>>

}