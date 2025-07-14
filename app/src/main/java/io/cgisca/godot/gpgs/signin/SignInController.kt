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


    fun signOut() {
    val signInClient = PlayGames.getGamesSignInClient(activity)
    signInClient.signOut().addOnCompleteListener { task ->
        if (task.isSuccessful) {
            signInListener.onSignOutSuccess()
        } else {
            signInListener.onSignOutFailed()
        }
    }
}


    fun isSignedIn(): Boolean {
        return connectionController.isConnected().first
    }

    private fun enablePopUps() {
        if (showPlayPopups) {
            PlayGames.getGamesClient(activity).setViewForPopups(activity.findViewById(android.R.id.content))
        }
    }
}
