package com.vroomvroomrider.android.repository.auth

import com.google.android.gms.tasks.OnCompleteListener
import com.vroomvroomrider.android.view.resource.Resource

interface AuthRepository {

    suspend fun register(): Resource<Boolean>?
    suspend fun generateEmailOtp(emailAddress: String): Resource<Boolean>
    suspend fun verifyEmailOtp(emailAddress: String, otp: String): Resource<Boolean>

}