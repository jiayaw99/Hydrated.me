package com.jiuntian.hydratedme.controller

import android.app.Activity
import android.content.Context
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.games.Games
import com.jiuntian.hydratedme.R

class GmsController {
    companion object {
        private const val RC_HP_LEADERBOARD_UI = 9004
        private const val RC_EXP_LEADERBOARD_UI = 9005
        private const val RC_ALL_LEADERBOARD_UI = 9006
        private const val RC_SIGN_IN = 9007

        fun submitHpAndExp(context: Context, hp: Int, exp: Int) {
            submitHp(context, hp)
            submitExp(context, exp)
        }

        fun submitHp(context: Context, hp: Int) {
            GoogleSignIn.getLastSignedInAccount(context)
                ?.let {
                    Games.getLeaderboardsClient(context, it)
                        .submitScore(context.getString(R.string.leaderboard_hp), hp.toLong())
                }
        }

        fun submitExp(context: Context, exp: Int) {
            GoogleSignIn.getLastSignedInAccount(context)
                ?.let {
                    Games.getLeaderboardsClient(context, it)
                        .submitScore(context.getString(R.string.leaderboard_exp), exp.toLong())
                }
        }

        fun showHpLeaderboard(context: Activity) {
            showLeaderboard(context, context.getString(R.string.leaderboard_hp), RC_HP_LEADERBOARD_UI)
        }

        fun showExpLeaderboard(context: Activity) {
            showLeaderboard(context, context.getString(R.string.leaderboard_exp), RC_EXP_LEADERBOARD_UI)
        }

        fun showAllLeaderboard(context: Activity) {
            GoogleSignIn.getLastSignedInAccount(context)?.let { googleSignInAccount ->
                Games.getLeaderboardsClient(context, googleSignInAccount)
                    .allLeaderboardsIntent
                    .addOnSuccessListener {
                        context.startActivityForResult(it, RC_ALL_LEADERBOARD_UI)
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed. ${it.localizedMessage}", Toast.LENGTH_LONG)
                            .show()
                    }
            } ?: run {
                Toast.makeText(context, "Failed, your should sign in your google account.", Toast.LENGTH_LONG)
                    .show()
                askGoogleSignIn(context)
            }
        }

        fun askGoogleSignIn(context: Activity) {
            val signInOptions = GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (GoogleSignIn.hasPermissions(account, *signInOptions.scopeArray)){
                // Already sign in
            } else {
                // try silent sign in
                val signInClient = GoogleSignIn.getClient(context, signInOptions)
                signInClient
                    .silentSignIn()
                    .addOnFailureListener {
                        Toast.makeText(context, "Google sign in failed. ${it.message}", Toast.LENGTH_LONG).show()
                        context.startActivityForResult(signInClient.signInIntent, RC_SIGN_IN)
                    }
            }

        }

        private fun showLeaderboard(context: Activity, leaderBoardId: String, requestCode: Int) {
            GoogleSignIn.getLastSignedInAccount(context)?.let { googleSignInAccount ->
                Games.getLeaderboardsClient(context, googleSignInAccount)
                    .getLeaderboardIntent(leaderBoardId)
                    .addOnSuccessListener {
                        context.startActivityForResult(it, requestCode)
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed. ${it.localizedMessage}", Toast.LENGTH_LONG)
                            .show()
                    }
            } ?: run {
                Toast.makeText(context, "Failed, your should sign in your google account.", Toast.LENGTH_LONG)
                    .show()
                askGoogleSignIn(context)
            }
        }
    }
}