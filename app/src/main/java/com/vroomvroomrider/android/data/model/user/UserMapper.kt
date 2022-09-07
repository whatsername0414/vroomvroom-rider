package com.vroomvroomrider.android.data.model.user

import com.vroomvroomrider.android.data.model.DomainMapper

class UserMapper : DomainMapper<UserDto, UserEntity> {
    override fun mapToDomainModel(model: UserDto): UserEntity {
        return UserEntity(
            model._id,
            model.name.orEmpty(),
            model.email.orEmpty(),
            model.pickedOrder,
            PhoneEntity(model.phone?.number, model.phone?.verified ?: false)
        )
    }

    override fun mapToDomainModelList(model: List<UserDto>): List<UserEntity> {
        TODO("Not yet implemented")
    }
}