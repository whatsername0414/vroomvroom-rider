package com.vroomvroomrider.android.data.model.order

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Order(
    val id: String,
    val customer: Customer,
    val merchant: Merchant,
    val payment: Payment,
    val deliveryAddress: DeliveryAddress,
    val orderDetail: OrderDetail,
    val status: Status,
    val createdAt: String,
    val comment: String
) : Parcelable

@Parcelize
data class Customer(
    val name: String,
    val phone: Phone?
) : Parcelable

@Parcelize
data class Phone(
    val number: String,
    val verified: Boolean
) : Parcelable

@Parcelize
data class Merchant(
    val id: String,
    val name: String,
    val location: List<Double>,
) : Parcelable

@Parcelize
data class Payment(
    val method: String,
    val createdAt: String
) : Parcelable

@Parcelize
data class DeliveryAddress (
    val address: String?,
    val city: String,
    val addInfo: String,
    val coordinates: List<Double>
) : Parcelable

@Parcelize
data class OrderDetail (
    val deliveryFee : Double,
    val totalPrice : Double,
    val totalItem: Int,
    val products : List<OrderProduct>,
) : Parcelable

@Parcelize
data class OrderProduct (
    val id: String,
    val productId : String,
    val name : String,
    val productImgUrl : String,
    val price : Double,
    val quantity : Int,
    val instructions : String,
    val options : List<OrderProductOption>?
) : Parcelable

@Parcelize
data class OrderProductOption (
    val name: String,
    val additionalPrice: Double,
    val optionType: String
) : Parcelable

@Parcelize
data class Status (
    val label: String,
    val ordinal: Int
) : Parcelable