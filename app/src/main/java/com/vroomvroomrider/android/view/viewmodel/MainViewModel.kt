package com.vroomvroomrider.android.view.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(

): ViewModel() {

    private val _isHomeScrolled by lazy { MutableLiveData(false) }
    val isHomeScrolled: LiveData<Boolean>
        get() = _isHomeScrolled
    private val _shouldBackToTop by lazy { MutableLiveData(false) }
    val shouldBackToTop: LiveData<Boolean>
        get() = _shouldBackToTop

    fun setIsHomeScroll(value: Boolean) {
        _isHomeScrolled.postValue(value)
    }

    fun setShouldBackToTop(value: Boolean) {
        _shouldBackToTop.postValue(value)
    }

}