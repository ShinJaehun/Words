package com.shinjaehun.words.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.shinjaehun.words.R
import com.shinjaehun.words.data.providers.PreferenceProvider
import com.shinjaehun.words.data.utils.FirebaseUtil
import com.shinjaehun.words.data.utils.GoogleUtil
import java.lang.Exception

private const val TAG = "SettingsFragment"

class SettingsFragment: PreferenceFragmentCompat() {

//    private lateinit var mGoogleSignInClient: GoogleSignInClient

    private lateinit var mProvider: PreferenceProvider

    private lateinit var mContext: Context

    private lateinit var mButton: Preference

    private lateinit var mToggle: Preference

//    private var mGoogleApiClient : GoogleApiClient? = null

//    private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//        .requestEmail().build()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) =
        addPreferencesFromResource(R.xml.preferences)


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Settings"
        (activity as? AppCompatActivity)?.supportActionBar?.subtitle = null

        mContext = activity?.applicationContext ?: requireContext().applicationContext

//        mGoogleApiClient = GoogleUtil.getGoogleApiClient(requireActivity())
//
//        mGoogleSignInClient = GoogleSignIn.getClient(mContext, gso)

        mButton = findPreference("SIGN_IN")!!

        mToggle = findPreference("SYNC")!!

        mProvider = PreferenceProvider(mContext)

//        if (FirebaseUtil.uid == null)
        checkTurningOff()

        mButton.setOnPreferenceClickListener {
            setButtonListener()
            return@setOnPreferenceClickListener true
        }

        checkForSigning()

    }

    private fun setButtonListener(){
        mButton.isEnabled = false
//        if (FirebaseUtil.uid == null)
        signIn()
//        else signOut()
    }

    private fun signIn() {
        Log.i(TAG, "signIn")
//        GoogleUtil.signInGoogle(mContext){
//            startActivityForResult(it, GoogleUtil.RC_GOOGLE_SIGN_IN)
//        }
    }

    private fun signOut() {
        Log.i(TAG, "signOut")
//        GoogleUtil.signOutGoogle(mGoogleApiClient) {
//            checkForSigning()
//            checkTurningOff()
//        }
    }

    private fun checkTurningOff() {
        (mToggle as SwitchPreference).isChecked = false
        mToggle.isEnabled = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.i(TAG, "onActivityResult")
//        if (requestCode == GoogleUtil.RC_GOOGLE_SIGN_IN) {
//            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
//            try {
//                GoogleUtil.handleActivityResult(task, mContext) {
//                    checkForSigning()
//                    initPrefs()
//                }
//            } catch (e: ApiException) {
//                catchExceptionFun("ApiException", e)
//            } catch (e: GoogleUserException) {
//                catchExceptionFun("GoogleException", e)
//            }
//        }

    }

    private fun catchExceptionFun(text: String, e: Exception) {
        Log.w(GoogleUtil.TAG, text, e)
        mButton.isEnabled = true
    }


    private fun initPrefs() {
        mProvider.isSyncNeeded = true
        mProvider.isSyncFirstLoad = true
//        mProvider.currentSyncUid = FirebaseUtil.uid
        mProvider.currentSyncUid = null // 이렇게 해도 되나요?
        mToggle.isEnabled = true
        (mToggle as SwitchPreference).isChecked = true
    }

    private fun checkForSigning() {
        mButton.isEnabled = true
        Log.i(TAG, "checkForSigning")
//        if (FirebaseUtil.uid != null) {
//            configurateSigning(
//                "Sign Out Google",
//                null,
//                GoogleUtil.getGoogleUser(mContext)?.displayName
//            )
//        } else {
//            configurateSigning(
//                "Sign In Google",
//                "for sync and other features",
//                "offline"
//            )
//        }
    }

    private fun configurateSigning(
        title: String?,
        summary: String?,
        subtitle: String?
    ) {
        mButton.title = title
        mButton.summary = summary
        (activity as? AppCompatActivity)?.supportActionBar?.subtitle = subtitle
    }

    override fun onPause() {
        super.onPause()
        Log.i(TAG, "onPause()")
//        mGoogleApiClient?.stopAutoManage(requireActivity())
//        mGoogleApiClient?.disconnect()
    }

}