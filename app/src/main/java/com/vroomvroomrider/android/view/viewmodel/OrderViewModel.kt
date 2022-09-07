package com.vroomvroomrider.android.view.viewmodel

import android.util.Log
import androidx.lifecycle.*
import com.vroomvroomrider.android.data.model.order.Order
import com.vroomvroomrider.android.repository.order.OrderRepository
import com.vroomvroomrider.android.view.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val repository: OrderRepository
): ViewModel() {

    private val _orders by lazy { MutableLiveData<Resource<List<Order>>>() }
    val orders: LiveData<Resource<List<Order>>>
        get() = _orders

    private val _order by lazy { MutableLiveData<Resource<Order>>() }
    val order: LiveData<Resource<Order>>
        get() = _order

    private var ordersJob: Job? = null
    var pickedOrder: String? = null
    var isActive = true

    fun getOrders() {
        if (ordersJob?.isActive == true) {
            Log.d("OrderViewModel", "Called")
            return
        }
        _orders.postValue(Resource.Loading)
        ordersJob = viewModelScope.launch(Dispatchers.IO) {
            while(this@OrderViewModel.isActive && orders.value != Resource.Loading) {
                val response = repository.getOrders()
                response?.let { data ->
                    when(data) {
                        is Resource.Success -> {
                            _orders.postValue(data)
                        }
                        is Resource.Error -> {
                            _orders.postValue(data)
                            ordersJob?.cancel()

                        }
                        else -> {
                            _orders.postValue(data)
                            ordersJob?.cancel()
                        }
                    }
                    delay(1000)
                }
            }
        }
    }

    fun getOrder(id: String) {
        _order.postValue(Resource.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            val response = repository.getOrder(id)
            response?.let { data ->
                when (data) {
                    is Resource.Success -> {
                        _order.postValue(data)
                    }
                    is Resource.Error -> {
                        _order.postValue(data)
                    }
                    else -> {
                        _order.postValue(data)
                    }
                }
            }
        }
    }

    fun pickOrder(id: String) {
        _order.postValue(Resource.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            val response = repository.pickOrder(id)
            response?.let { data ->
                when (data) {
                    is Resource.Success -> {
                        _order.postValue(data)
                    }
                    is Resource.Error -> {
                        _order.postValue(data)
                    }
                    else -> {
                        _order.postValue(data)
                    }
                }
            }
        }
    }

    fun purchasedOrder(id: String) {
        _order.postValue(Resource.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            val response = repository.purchasedOrder(id)
            response?.let { data ->
                when (data) {
                    is Resource.Success -> {
                        _order.postValue(data)
                    }
                    is Resource.Error -> {
                        _order.postValue(data)
                    }
                    else -> {
                        _order.postValue(data)
                    }
                }
            }
        }
    }

    fun arrived(id: String) {
        _order.postValue(Resource.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            val response = repository.arrived(id)
            response?.let { data ->
                when (data) {
                    is Resource.Success -> {
                        _order.postValue(data)
                    }
                    is Resource.Error -> {
                        _order.postValue(data)
                    }
                    else -> {
                        _order.postValue(data)
                    }
                }
            }
        }
    }

    fun deliveredOrder(id: String) {
        _order.postValue(Resource.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            val response = repository.deliveredOrder(id)
            response?.let { data ->
                when (data) {
                    is Resource.Success -> {
                        _order.postValue(data)
                    }
                    is Resource.Error -> {
                        _order.postValue(data)
                    }
                    else -> {
                        _order.postValue(data)
                    }
                }
            }
        }
    }

    fun cancelJobs() {
        ordersJob?.cancel()
    }

    fun startJobs() {
        ordersJob?.start()
    }

}