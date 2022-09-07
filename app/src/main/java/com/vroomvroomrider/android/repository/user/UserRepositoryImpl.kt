package com.vroomvroomrider.android.repository.user

import androidx.lifecycle.LiveData
import com.vroomvroomrider.android.data.db.UserDao
import com.vroomvroomrider.android.data.model.user.UserEntity
import com.vroomvroomrider.android.repository.base.BaseRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
): UserRepository, BaseRepository() {

    override fun getUserLocale(): LiveData<UserEntity> {
        return userDao.getUser()
    }

    override suspend fun updatePickedOrder(id: String, orderId: String?) {
        return userDao.updatePickedOrder(id, orderId)
    }

}