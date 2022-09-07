package com.vroomvroomrider.android.data.model

interface DomainMapper<T, DomainModel> {
    fun mapToDomainModel(model: T): DomainModel
    fun mapToDomainModelList(model: List<T>): List<DomainModel>
}