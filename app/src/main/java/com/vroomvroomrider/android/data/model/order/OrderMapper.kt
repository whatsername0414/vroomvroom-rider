package com.vroomvroomrider.android.data.model.order

import com.vroomvroomrider.android.data.model.DomainMapper
import com.vroomvroomrider.android.utils.Constants.FORMAT_DD_MMM_YYYY_HH_MM_SS
import com.vroomvroomrider.android.utils.Utils.formatStringToDate

class OrderMapper: DomainMapper<OrderDto, Order> {
    override fun mapToDomainModel(model: OrderDto): Order {
        return Order(
            id = model.id,
            customer = mapToCustomer(model.customerDto),
            merchant = mapToMerchant(model.merchantDto),
            payment = Payment(model.paymentDto.method, model.createdAt),
            deliveryAddress = mapToDeliveryAddress(model.deliveryAddressDto),
            orderDetail = mapToOrderDetail(model.orderDetail),
            status = mapToStatus(model.status),
            createdAt = formatStringToDate(model.createdAt, FORMAT_DD_MMM_YYYY_HH_MM_SS),
            comment = model.comment.orEmpty()
        )
    }

    override fun mapToDomainModelList(model: List<OrderDto>): List<Order> {
        return model.map {
            Order(
                id = it.id,
                customer = mapToCustomer(it.customerDto),
                merchant = mapToMerchant(it.merchantDto),
                payment = Payment(it.paymentDto.method, it.createdAt),
                deliveryAddress = mapToDeliveryAddress(it.deliveryAddressDto),
                orderDetail = mapToOrderDetail(it.orderDetail),
                status = mapToStatus(it.status),
                createdAt = formatStringToDate(it.createdAt, FORMAT_DD_MMM_YYYY_HH_MM_SS),
                comment = it.comment.orEmpty()
            )
        }
    }

    private fun mapToCustomer(customerDto: CustomerDto): Customer {
        customerDto.apply {
            return Customer(
                name = name.orEmpty(),
                Phone(
                    number = phone?.number.orEmpty(),
                    verified = phone?.verified ?: false
                ))
        }
    }

    private fun mapToMerchant(merchantDto: MerchantDto): Merchant {
        merchantDto.apply {
            return Merchant(
                id = id,
                name = name,
                location = location
            )
        }
    }

    private fun mapToDeliveryAddress(deliveryAddressDto: DeliveryAddressDto): DeliveryAddress {
        deliveryAddressDto.apply {
            return DeliveryAddress(
                address = address,
                city = city.orEmpty(),
                addInfo = addInfo.orEmpty(),
                coordinates = coordinates
            )
        }
    }

    private fun mapToOrderDetail(orderDetailDto: OrderDetailDto): OrderDetail {
        orderDetailDto.apply {
            var totalItem = 0
            orderDetailDto.products.forEach {
                totalItem += it.quantity }
            return OrderDetail(
                deliveryFee = deliveryFee,
                totalPrice = totalPrice,
                totalItem = totalItem,
                products = mapToProductList(orderDetailDto.products)
            )
        }
    }

    private fun mapToProductList(orderProductsDto: List<OrderProductDto>): List<OrderProduct> {
        return orderProductsDto.map {
            OrderProduct(
                id = it.id,
                productId = it.productId,
                name = it.name,
                productImgUrl = it.productImgUrl.orEmpty(),
                price = it.price,
                quantity = it.quantity,
                instructions = it.instructions.orEmpty(),
                options = mapToOption(it.options)
            )
        }
    }

    private fun mapToOption(
        orderProductOptionsDto: List<OrderProductOptionDto>?
    ): List<OrderProductOption>? {
        return orderProductOptionsDto?.map {
            OrderProductOption(
                name = it.name,
                additionalPrice = it.additionalPrice ?: 0.0,
                optionType = it.optionType
            )
        }
    }

    private fun mapToStatus(statusDto: StatusDto): Status {
        statusDto.apply {
            return Status(
                label = label,
                ordinal = ordinal
            )
        }
    }
}