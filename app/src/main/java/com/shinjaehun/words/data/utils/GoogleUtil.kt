package com.shinjaehun.words.data.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.shinjaehun.words.BuildConfig
import com.shinjaehun.words.R
import com.shinjaehun.words.data.db.entity.GoogleUser
import kotlinx.coroutines.coroutineScope
import kotlin.Exception

private const val TAG = "GoogleUtil"

object GoogleUtil {

    const val TAG = "GoogleUtil"
    const val RC_GOOGLE_SIGN_IN = 9000

    suspend fun signInGoogle(
        context: Context,
        onComplete: () -> Unit
    ) {
        val credentialManager: CredentialManager = CredentialManager.create(context)

        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
//            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(BuildConfig.API_KEY)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        coroutineScope {
            try {
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                if (credential is CustomCredential
                    && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                    val c = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                    val createGoogleUserResult = FirebaseUtil.auth.signInWithCredential(c)
                    createGoogleUserResult
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.i(TAG, "signInWithCredential:success")
                                onComplete()
                            } else {
                                Log.e(TAG, "signInWithCredential:failure", task.exception)
                            }
                        }
                } else {
                    Log.e(TAG, "login error with credential")
                }
            } catch (e: Exception) {
                println(e)
//                Log.e(TAG, e.toString())
            }
        }
    }

    fun signOutGoogle(onComplete: () -> Unit = {}) {
        FirebaseUtil.auth.signOut()
        onComplete()
    }

// 아무튼 이거 전부 deprecated...
//    fun signInGoogle(
//        ctx: Context,
//        idToken: String? = ctx.getString(R.string.token_id),
//        options: GoogleSignInOptions = GoogleSignInOptions.DEFAULT_SIGN_IN,
//        onComplete: (Intent) -> Unit
//    ) {
//        val signInActivity = GoogleSignIn.getClient(
//            ctx, GoogleSignInOptions.Builder(options).requestIdToken(idToken)
//                .requestEmail().build()
//        ).signInIntent
//        onComplete(signInActivity)
//    }
//
//    fun handleActivityResult(
//        task: Task<GoogleSignInAccount>,
//        context: Context,
//        onComplete: () -> Unit = {}
//    ) {
//        val account = task.getResult(ApiException::class.java)
//        val credential = GoogleAuthProvider.getCredential(account!!.idToken, null)
//        FirebaseUtil.auth.signInWithCredential(credential).addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//                FirebaseUtil.currentUserDocRef.get().addOnSuccessListener { cur ->
//                    if (!cur.exists()) {
//                        val newUser = GoogleUtil.getGoogleUser(context)
//                        FirebaseUtil.currentUserDocRef.set(newUser ?: throw GoogleUserException())
//                            .addOnSuccessListener {
//                                onComplete()
//                            }
//                    } else {
//                        onComplete()
//                    }
//                }
//            }
//        }
//
//    }
//
//    fun signOutGoogle(googleApiClient: GoogleApiClient?, onComplete: () -> Unit = {}) {
//        try {
//            Auth.GoogleSignInApi.signOut(googleApiClient)
//        } catch (e: Exception) {
//            Log.w(TAG, "Sign Out", e)
//        }
//        FirebaseUtil.auth.signOut()
//        onComplete()
//    }
//
//    fun getGoogleUser(ctx: Context): GoogleUser? {
//        var user: GoogleUser? = null
//        googleUserBuff(ctx) {
//            user = it
//        }
//        return user
//    }
//
//    fun getGoogleApiClient(activity: FragmentActivity) : GoogleApiClient? =
//        GoogleApiClient.Builder(activity).enableAutoManage(activity) {
//            Log.d("TAG", "Connection failed:$it")
//        }.addApi(Auth.GOOGLE_SIGN_IN_API).build()
//
//
//
//    private fun googleUserBuff(ctx: Context, onComplete: (GoogleUser) -> Unit) {
//        GoogleSignIn.getLastSignedInAccount(ctx)?.let {
//            onComplete(
//                GoogleUser(
//                    it.displayName,
//                    it.givenName,
//                    it.familyName,
//                    it.email,
//                    it.id,
//                    it.photoUrl?.toString(),
//                    FirebaseUtil.uid
//                )
//            )
//        }
//    }

}