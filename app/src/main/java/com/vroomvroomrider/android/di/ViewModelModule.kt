package com.vroomvroomrider.android.di

import com.vroomvroomrider.android.repository.auth.AuthRepository
import com.vroomvroomrider.android.repository.auth.AuthRepositoryImpl
import com.vroomvroomrider.android.repository.auth.FirebaseRepository
import com.vroomvroomrider.android.repository.auth.FirebaseRepositoryImpl
import com.vroomvroomrider.android.repository.order.OrderRepository
import com.vroomvroomrider.android.repository.order.OrderRepositoryImpl
import com.vroomvroomrider.android.repository.user.UserRepository
import com.vroomvroomrider.android.repository.user.UserRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
abstract class ViewModelModule {

    @Binds
    @ViewModelScoped
    abstract fun bindOrderRepository(repo: OrderRepositoryImpl): OrderRepository

    @Binds
    @ViewModelScoped
    abstract fun userRepository(repo: UserRepositoryImpl) : UserRepository

    @Binds
    @ViewModelScoped
    abstract fun authRepository(repo: AuthRepositoryImpl) : AuthRepository

    @Binds
    @ViewModelScoped
    abstract fun firebaseAuthRepository(repo: FirebaseRepositoryImpl) : FirebaseRepository

}