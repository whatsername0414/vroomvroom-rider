package com.vroomvroomrider.android.data.model.order

import com.google.gson.annotations.SerializedName

data class OrderDto(
    @SerializedName("_id") val id: String,
    @SerializedName("customer") val customerDto: CustomerDto,
    @SerializedName("merchant") val merchantDto: MerchantDto,
    @SerializedName("payment") val paymentDto: PaymentDto,
    @SerializedName("delivery_address") val deliveryAddressDto: DeliveryAddressDto,
    @SerializedName("order_detail") val orderDetail: OrderDetailDto,
    val status: StatusDto,
    @SerializedName("created_at") val createdAt: String,
    val comment: String?
)

data class CustomerDto(
    val name: String?,
    val phone: PhoneDto?
)

data class PhoneDto(
    val number: String,
    val verified: Boolean
)

data class MerchantDto(
    @SerializedName("_id") val id: String,
    val name: String,
    val location: List<Double>
)

data class PaymentDto(
    val method: String,
    @SerializedName("created_at") val createdAt: String
)

data class DeliveryAddressDto (
    val address: String?,
    val city: String?,
    @SerializedName("additional_information") val addInfo: String? = null,
    val coordinates: List<Double>
)

data class OrderDetailDto (
    @SerializedName("delivery_fee") val deliveryFee : Double,
    @SerializedName("total_price") val totalPrice : Double,
    val products : List<OrderProductDto>,
)

data class OrderProductDto (
    @SerializedName("_id") val id: String,
    @SerializedName("product_id") val productId : String,
    val name : String,
    @SerializedName("product_img_url") val productImgUrl : String?,
    val price : Double,
    val quantity : Int,
    val instructions : String? = null,
    val options : List<OrderProductOptionDto>?
)

data class OrderProductOptionDto(
    val name: String,
    @SerializedName("additional_price") val additionalPrice: Double?,
    @SerializedName("option_type") val optionType: String
)

data class StatusDto (
    val label: String,
    val ordinal: Int
)