package com.vroomvroomrider.android.repository.auth

import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.vroomvroomrider.android.repository.base.BaseRepository
import com.vroomvroomrider.android.view.resource.Resource
import javax.inject.Inject

class FirebaseRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val googleSignInClient: GoogleSignInClient
) : FirebaseRepository, BaseRepository() {
    override fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
    override fun logoutUser(listener: OnCompleteListener<Void>) {
        auth.signOut()
        googleSignInClient.signOut().addOnCompleteListener(listener)
    }
    override fun removeAuthStateListener(listener: FirebaseAuth.AuthStateListener) {
        auth.removeAuthStateListener(listener)
    }
    override fun getIdToken(onResult: (String?)->Unit) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            user.getIdToken(true)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result.token
                        onResult(token)

                    } else onResult(null)
                }
        }
    }

    override fun signInIntent()  = googleSignInClient.signInIntent

    override fun googleSignIn(data: Intent?, onResult: (Resource<FirebaseUser>) -> Unit) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        val exception = task.exception
        if (task.isSuccessful) {
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account?.idToken.orEmpty(), onResult)
            } catch (e: ApiException) {
                onResult(handleException(401))
                Log.w(TAG, "Google sign in failed", e)
            }
        } else {
            onResult(handleException(401))
            Log.d(TAG, exception.toString())
        }
    }

    override fun firebaseAuthWithGoogle(
        idToken: String,
        onResult: (Resource<FirebaseUser>) -> Unit
    ) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                val exception = task.exception
                if (task.isSuccessful) {
                    auth.currentUser?.let { user ->
                        onResult(handleSuccess(user))
                    }
                } else {
                    onResult(handleException(401))
                    Log.d(TAG, exception.toString())
                }
            }
    }

    override fun logInWithEmailAndPassword(
        emailAddress: String,
        password: String,
        onResult: (Resource<FirebaseUser>) -> Unit
    ) {
        auth.signInWithEmailAndPassword(emailAddress, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    auth.currentUser?.let { user ->
                        onResult(handleSuccess(user))
                    }
                } else {
                    val exception = task.exception?.message
                    onResult(handleException(401, exception))
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                }
            }
    }
    override fun registerWithEmailAndPassword(
        emailAddress: String,
        password: String,
        onResult: (Resource<FirebaseUser>) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(emailAddress, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    auth.currentUser?.let { user ->
                        onResult(handleSuccess(user))
                    }
                } else {
                    onResult(handleException(409))
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                }
            }
    }

    override fun resetPasswordWithEmail(
        emailAddress: String,
        onSent: (Resource<String>) -> Unit
    ) {
        auth.sendPasswordResetEmail(emailAddress)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSent(handleSuccess("Email sent"))
                } else onSent(handleException(0))
            }
    }

    companion object {
        const val TAG = "FirebaseRepositoryImpl"
    }
}