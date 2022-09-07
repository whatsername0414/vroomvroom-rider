package com.vroomvroomrider.android.repository.auth

import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.vroomvroomrider.android.data.api.AuthService
import com.vroomvroomrider.android.data.db.UserDao
import com.vroomvroomrider.android.data.model.user.UserMapper
import com.vroomvroomrider.android.repository.base.BaseRepository
import com.vroomvroomrider.android.view.resource.Resource
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val service: AuthService,
    private val userDao: UserDao,
    private val userMapper: UserMapper
) : AuthRepository, BaseRepository() {

    override suspend fun register(): Resource<Boolean>? {
        var data: Resource<Boolean>? = null
        try {
            val body = mapOf("type" to "rider")
            val result = service.register(body)
            if (result.isSuccessful && result.code() == 200) {
                result.body()?.data?.let {
                    val user = userMapper.mapToDomainModel(it)
                    userDao.insertUser(user)
                    data = handleSuccess(true)
                }
            } else {
                return handleException(result.code(), result.errorBody())
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error: ${e.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return data
    }

    override suspend fun generateEmailOtp(emailAddress: String): Resource<Boolean> {
        val data: Resource<Boolean>
        try {
            val body = mapOf("emailAddress" to emailAddress)
            val result = service.generateEmailOtp(body)
            if (result.isSuccessful && result.code() == 200) {
                data = handleSuccess(true)
            } else {
                return handleException(result.code(), result.errorBody())
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error: ${e.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return data
    }

    override suspend fun verifyEmailOtp(emailAddress: String, otp: String): Resource<Boolean> {
        val data: Resource<Boolean>
        try {
            val body = mapOf(
                "emailAddress" to emailAddress,
                "otp" to otp
            )
            val result = service.verifyEmailOtp(body)
            if (result.isSuccessful && result.code() == 200) {
                data = handleSuccess(true)
            } else {
                return handleException(result.code(), result.errorBody())
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error: ${e.message}")
            return handleException(GENERAL_ERROR_CODE)
        }
        return data
    }

    companion object {
        const val TAG = "AuthRepositoryImpl"
    }
}