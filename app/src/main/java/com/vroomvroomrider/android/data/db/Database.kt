package com.vroomvroomrider.android.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.vroomvroomrider.android.data.model.user.UserEntity

@Database(
    entities = [UserEntity::class, ],
    version = 1,
    exportSchema = false
)
abstract class Database : RoomDatabase() {
    abstract fun userDao(): UserDao

}