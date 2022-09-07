package com.vroomvroomrider.android.data.model.order

enum class StatusEnum(val label: String) {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    ACCEPTED("Accepted"),
    PURCHASED("Purchased"),
    ARRIVED("Arrived"),
    DELIVERED("Delivered")
}