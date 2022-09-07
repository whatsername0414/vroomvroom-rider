package com.vroomvroomrider.android.view.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vroomvroomrider.android.repository.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
): ViewModel() {

    val user = userRepository.getUserLocale()

    fun updatePickedOrder(id: String, order: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.updatePickedOrder(id, order)
        }
    }

}