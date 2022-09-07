package com.vroomvroomrider.android.view.viewmodel

import android.content.Intent
import androidx.lifecycle.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseUser
import com.vroomvroomrider.android.data.preferences.Preferences
import com.vroomvroomrider.android.repository.auth.AuthRepository
import com.vroomvroomrider.android.repository.auth.FirebaseRepository
import com.vroomvroomrider.android.view.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val firebaseRepository: FirebaseRepository,
    private val preferences: Preferences
): ViewModel() {

    private val _newLoggedInUser by lazy { MutableLiveData<Resource<FirebaseUser>>() }
    val newLoggedInUser: LiveData<Resource<FirebaseUser>>
        get() = _newLoggedInUser

    private val _isRegistered by lazy { MutableLiveData<Resource<Boolean>>() }
    val isRegistered: LiveData<Resource<Boolean>>
        get() = _isRegistered

    private val _isOtpSent by lazy { MutableLiveData<Resource<Boolean>>() }
    val isOtpSent: LiveData<Resource<Boolean>>
        get() = _isOtpSent

    private val _isVerified by lazy { MutableLiveData<Resource<Boolean>>() }
    val isVerified: LiveData<Resource<Boolean>>
        get() = _isVerified

    val token = preferences.token.asLiveData()
    val signInIntent = firebaseRepository.signInIntent()

    fun saveIdToken() {
        firebaseRepository.getIdToken { result ->
            result?.let { token ->
                viewModelScope.launch(Dispatchers.IO) {
                    preferences.saveToken(token)
                }
            }
        }
    }

    fun resetOtpLiveData() {
        _isOtpSent.postValue(null)
    }

    fun clearDataStore() {
        viewModelScope.launch(Dispatchers.IO) {
            preferences.clear()
        }
    }

    fun getLocalIdToken() {
        viewModelScope.launch(Dispatchers.IO) {
            preferences.token.first()
        }
    }

    fun register() {
        _isRegistered.postValue(Resource.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            val response = authRepository.register()
            response.let { data ->
                when (data) {
                    is Resource.Success -> {
                        _isRegistered.postValue(data)
                    }
                    is Resource.Error -> {
                        _isRegistered.postValue(data)
                    }
                    else -> {
                        _isRegistered.postValue(data)
                    }
                }
            }
        }
    }

    fun generateEmailOtp(emailAddress: String) {
        _isOtpSent.postValue(Resource.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            val response = authRepository.generateEmailOtp(emailAddress)
            when (response) {
                is Resource.Success ->
                    _isOtpSent.postValue(response)
                is Resource.Error -> {
                    _isOtpSent.postValue(response)
                }
                else -> Unit
            }
        }
    }

    fun verifyEmailOtp(emailAddress: String, otp: String) {
        _isVerified.postValue(Resource.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            val response = authRepository.verifyEmailOtp(emailAddress, otp)
            when (response) {
                is Resource.Success -> {
                    _isVerified.postValue(response)
                }
                is Resource.Error -> {
                    _isVerified.postValue(response)
                }
                else -> {
                    _isVerified.postValue(response)
                }
            }
        }
    }

    fun googleSignIn(data: Intent?) {
        _newLoggedInUser.postValue(Resource.Loading)
        firebaseRepository.googleSignIn(data) { result ->
            when (result) {
                is Resource.Success -> {
                    _newLoggedInUser.postValue(result)
                }
                is Resource.Error -> {
                    _newLoggedInUser.postValue(result)
                }
                else -> {
                    _newLoggedInUser.postValue(result)
                }
            }
        }
    }

    fun registerWithEmailAndPassword(emailAddress: String, password: String) {
        _newLoggedInUser.postValue(Resource.Loading)
        firebaseRepository.registerWithEmailAndPassword(emailAddress, password) { result ->
            when (result) {
                is Resource.Success -> {
                    _newLoggedInUser.postValue(result)
                }
                is Resource.Error -> {
                    _newLoggedInUser.postValue(result)
                }
                else -> {
                    _newLoggedInUser.postValue(result)
                }
            }
        }
    }



    fun logInWithEmailAndPassword(emailAddress: String, password: String) {
        _newLoggedInUser.postValue(Resource.Loading)
        firebaseRepository.logInWithEmailAndPassword(emailAddress, password) { result ->
            when (result) {
                is Resource.Success -> {
                    _newLoggedInUser.postValue(result)
                }
                is Resource.Error -> {
                    _newLoggedInUser.postValue(result)
                }
                else -> {
                    _newLoggedInUser.postValue(result)
                }
            }
        }
    }


    fun logoutUser(successful: (Boolean) -> Unit) {
        val listener = OnCompleteListener<Void> { task ->
            if (task.isSuccessful) {
                _newLoggedInUser.postValue(null)
                successful.invoke(true)
            } else {
                successful.invoke(false)
            }
        }
        firebaseRepository.logoutUser(listener)
    }

}