package com.vroomvroomrider.android.di

import android.content.Context
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.auth.api.phone.SmsRetrieverClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.vroomvroomrider.android.R
import com.vroomvroomrider.android.data.api.AuthService
import com.vroomvroomrider.android.data.api.OrderService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    @Singleton
    @Provides
    fun provideOrderService(retrofit: Retrofit): OrderService {
        return retrofit.create(OrderService::class.java)
    }

    @Singleton
    @Provides
    fun provideAuthService(retrofit: Retrofit): AuthService {
        return retrofit.create(AuthService::class.java)
    }

    @Singleton
    @Provides
    fun provideGoogleSignInOptions(@ApplicationContext app: Context): GoogleSignInOptions = GoogleSignInOptions
        .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(app.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

    @Singleton
    @Provides
    fun provideGoogleSignInClient(
        @ApplicationContext app: Context,
        gso: GoogleSignInOptions
    ): GoogleSignInClient = GoogleSignIn.getClient(app, gso)

    @Singleton
    @Provides
    fun provideFirebaseAuth() = FirebaseAuth.getInstance()
}