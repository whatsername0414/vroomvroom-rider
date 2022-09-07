package com.vroomvroomrider.android.repository.auth

import android.content.Intent
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.vroomvroomrider.android.view.resource.Resource

interface FirebaseRepository {

    fun getCurrentUser(): FirebaseUser?
    fun logoutUser(listener: OnCompleteListener<Void>)
    fun removeAuthStateListener(listener: FirebaseAuth.AuthStateListener)
    fun getIdToken(onResult: (String?)->Unit)
    fun signInIntent(): Intent
    fun googleSignIn(data: Intent?, onResult: (Resource<FirebaseUser>) -> Unit)
    fun firebaseAuthWithGoogle(idToken: String, onResult: (Resource<FirebaseUser>) -> Unit)
    fun logInWithEmailAndPassword(emailAddress: String, password: String, onResult: (Resource<FirebaseUser>) -> Unit)
    fun registerWithEmailAndPassword(emailAddress: String, password: String, onResult: (Resource<FirebaseUser>) -> Unit)
    fun resetPasswordWithEmail(emailAddress: String, onSent: (Resource<String>) -> Unit)

}