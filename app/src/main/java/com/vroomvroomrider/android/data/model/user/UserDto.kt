package com.vroomvroomrider.android.data.model.user

import com.google.gson.annotations.SerializedName

data class UserDto(
    val _id: String,
    val name: String? = null,
    val email: String? = null,
    @SerializedName("picked_order") val pickedOrder: String? = null,
    val phone: PhoneDto? = null
)

data class PhoneDto(
    val number: String? = null,
    val verified: Boolean = false
)
