package com.vroomvroomrider.android.di

import com.vroomvroomrider.android.data.model.order.OrderMapper
import com.vroomvroomrider.android.data.model.user.UserMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MapperModule {

    @Singleton
    @Provides
    fun provideOrderMapper(): OrderMapper {
        return OrderMapper()
    }

    @Singleton
    @Provides
    fun provideUserMapper(): UserMapper {
        return UserMapper()
    }

}