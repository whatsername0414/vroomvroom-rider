package com.vroomvroomrider.android.data.model.user

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.vroomvroomrider.android.utils.Constants.USER_TABLE

@Entity(tableName = USER_TABLE)
data class UserEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val email: String,
    val pickedOrder: String? = null,
    @Embedded
    val phone: PhoneEntity? = null
)

data class PhoneEntity(
    val number: String? = null,
    val verified: Boolean = false
)