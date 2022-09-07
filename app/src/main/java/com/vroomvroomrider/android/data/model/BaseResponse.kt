package com.vroomvroomrider.android.data.model

data class BaseResponse<T>(
    val data: T
)

data class BaseResponseList<T>(
    val data: List<T>,
    val count: Int,
    val pages: Int
)