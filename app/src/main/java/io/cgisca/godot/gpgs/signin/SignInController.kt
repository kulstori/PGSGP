package io.cgisca.godot.gpgs.signin

import android.app.Activity
import android.util.Log
import android.util.Pair
import com.google.android.gms.games.PlayGames
import com.google.android.gms.games.GamesSignInClient
import com.google.android.gms.games.GamesSignInClient.Authenticated
import com.google.android.gms.games.Games
import io.cgisca.godot.gpgs.ConnectionController

class SignInController(
    private var activity: Activity,
    private var signInListener: SignInListener,
    private var connectionController: ConnectionController
) {

    companion object {
        const val RC_SIGN_IN = 77
    }

    private var showPlayPopups = true

    fun setShowPopups(enablePopUps: Boolean) {
        showPlayPopups = enablePopUps
    }

    fun signIn() {
    Log.i("godot", "Attempting to sign in (v2)")
    val userProfile = UserProfile(null, null, null, null, null)
    val signInClient = PlayGames.getGamesSignInClient(activity)

    signInClient.isAuthenticated.addOnCompleteListener { task ->
        val status = task.result
        if (status == Authenticated) {
            Log.i("godot", "Already signed in")
            signInListener.onSignedInSuccessfully(userProfile)
            enablePopUps()
        } else {
            Log.i("godot", "Launching sign-in UI")
            signInClient.signIn().addOnCompleteListener { signInTask ->
                if (signInTask.isSuccessful) {
                    Log.i("godot", "Sign-in success")
                    signInListener.onSignedInSuccessfully(userProfile)
                    enablePopUps()
                } else {
                    Log.e("godot", "Sign-in failed: ${signInTask.exception?.message}")
                    signInListener.onSignInFailed(-1)
                }
            }
        }
    }
}

    fun onSignInActivityResult(googleSignInResult: GoogleSignInResult?) {
        val userProfile = UserProfile(null, null, null, null, null)
        if (googleSignInResult != null && googleSignInResult.isSuccess) {
            val googleSignInAccount = googleSignInResult.signInAccount
            if (googleSignInAccount != null) {
                userProfile.let {
                    it.displayName = googleSignInAccount.displayName
                    it.email = googleSignInAccount.email
                    it.token = googleSignInAccount.idToken
                    it.authCode = googleSignInAccount.serverAuthCode
                    it.id = googleSignInAccount.id
                }
            }
            enablePopUps()
            signInListener.onSignedInSuccessfully(userProfile)
        } else {
            var statusCode = Int.MIN_VALUE
            googleSignInResult?.status?.let {
                statusCode = it.statusCode
            }
            signInListener.onSignInFailed(statusCode)
        }
    }

    fun signOut(googleSignInClient: GoogleSignInClient) {
        googleSignInClient.signOut().addOnCompleteListener(activity) { task ->
            if (task.isSuccessful) {
                signInListener.onSignOutSuccess()
            } else {
                signInListener.onSignOutFailed()
            }
        }
    }

    fun isSignedIn(): Boolean {
        val googleSignInAccount = GoogleSignIn.getLastSignedInAccount(activity)
        return connectionController.isConnected().first && googleSignInAccount != null
    }

    private fun enablePopUps() {
        if (showPlayPopups) {
            val lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(activity)
            if (lastSignedInAccount != null) {
                Games.getGamesClient(activity, lastSignedInAccount)
                    .setViewForPopups(activity.findViewById(android.R.id.content))
            }
        }
    }
}
