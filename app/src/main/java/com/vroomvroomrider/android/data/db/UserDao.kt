package com.vroomvroomrider.android.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.vroomvroomrider.android.data.model.user.UserEntity

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(userEntity: UserEntity)
    @Update
    suspend fun updateUser(userEntity: UserEntity)
    @Query("UPDATE user_table SET name = :name WHERE id = :id")
    suspend fun updateUserName(id: String, name: String)
    @Query("UPDATE user_table SET pickedOrder = :pickedOrder WHERE id = :id")
    suspend fun updatePickedOrder(id: String, pickedOrder: String?)
    @Query("DELETE FROM user_table")
    suspend fun deleteUser()
    @Transaction
    @Query("SELECT * FROM user_table")
    fun getUser(): LiveData<UserEntity>

}