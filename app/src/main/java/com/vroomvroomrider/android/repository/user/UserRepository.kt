package com.vroomvroomrider.android.repository.user

import androidx.lifecycle.LiveData
import com.vroomvroomrider.android.data.model.user.UserEntity

interface UserRepository {

    //RoomDB
    fun getUserLocale(): LiveData<UserEntity>
    suspend fun updatePickedOrder(id: String, orderId: String?)
}