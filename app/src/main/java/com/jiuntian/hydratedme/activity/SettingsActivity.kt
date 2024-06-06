package com.jiuntian.hydratedme.activity

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import com.dr1009.app.chronodialogpreference.ChronoPreferenceFragment
import com.google.firebase.auth.FirebaseAuth
import com.jiuntian.hydratedme.R
import com.jiuntian.hydratedme.controller.DrinkRecordsController
import com.jiuntian.hydratedme.controller.GmsController
import com.jiuntian.hydratedme.controller.PetDataController
import com.jiuntian.hydratedme.model.PetData
import com.jiuntian.hydratedme.ui.dialog.ChoosePetDialogFragment
import com.jiuntian.hydratedme.ui.dialog.OptimizeGoalDialogFragment
import com.jiuntian.hydratedme.util.RateLimiter
import com.jiuntian.hydratedme.util.Util

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : ChronoPreferenceFragment() {
        private lateinit var auth: FirebaseAuth
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            auth = FirebaseAuth.getInstance()

            val email: Preference? = findPreference(getString(R.string.preference_email))
            email!!.summary = auth.currentUser!!.email
            email.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { preference, newValue ->
                    auth.currentUser!!.updateEmail(newValue as String)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Email changed to $newValue", Toast.LENGTH_LONG).show()
                            email.summary = newValue
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Change email failed. ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                        .isSuccessful
                }

            val logoutButton: Preference? = findPreference(getString(R.string.preference_logout))
            logoutButton?.setOnPreferenceClickListener {
                auth.signOut()
                Log.i("setting:account", "Logged out")
                Toast.makeText(requireActivity().applicationContext, "Logged out", Toast.LENGTH_LONG).show()
                requireActivity().onBackPressed()
                true
            }

            val resetPasswordButton: Preference? = findPreference(getString(R.string.preference_reset_password))
            resetPasswordButton?.setOnPreferenceClickListener {
                auth.sendPasswordResetEmail(auth.currentUser!!.email!!).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            requireActivity().applicationContext,
                            "Password reset email has been sent.",
                            Toast.LENGTH_LONG
                        ).show()
                        Log.i("setting:account", "successful send reset password email")
                    }
                }.addOnFailureListener { exception ->
                    Toast.makeText(requireActivity().applicationContext, exception.localizedMessage, Toast.LENGTH_LONG).show()
                    Log.e("setting:account", exception.localizedMessage!!)
                }
                true
            }


            val petType: Preference? = findPreference(getString(R.string.preference_pet))
            PetDataController.firebaseUtils.firebaseUserDocument
                .get()
                .addOnSuccessListener { document ->
                        val record = document.toObject(PetData::class.java)
                        petType!!.summary = record!!.type.printableName
                }
            petType?.setOnPreferenceClickListener {
                ChoosePetDialogFragment(it).show(requireFragmentManager(), "SETTING")
                true
            }

            val intakeGoalEditText: EditTextPreference? = findPreference(getString(R.string.schedule_goal))
            intakeGoalEditText?.setOnBindEditTextListener { editText ->
                editText.inputType = InputType.TYPE_CLASS_NUMBER
            }

            val cupSizeEditText: EditTextPreference? = findPreference(getString(R.string.cup_size))
            cupSizeEditText?.setOnBindEditTextListener { editText ->
                editText.inputType = InputType.TYPE_CLASS_NUMBER
            }

            val optimizeGoalButton: Preference? = findPreference(getString(R.string.optimize_goal))
            optimizeGoalButton?.setOnPreferenceClickListener {
                OptimizeGoalDialogFragment().show(requireFragmentManager(), "SETTING")
                true
            }

            val signInGooglePlayButton: Preference? = findPreference(getString(R.string.sign_in_google_play))
            signInGooglePlayButton?.setOnPreferenceClickListener{
                GmsController.askGoogleSignIn(requireActivity())
                true
            }

            val fixReminderNotificationButton: Preference? = findPreference(getString(R.string.fix_reminder_notification))
            fixReminderNotificationButton?.setOnPreferenceClickListener {
                Util.getBatteryOptimizationSettingActivity(requireContext())
                true
            }

            val resetWaterIntakeLimitButton: Preference? = findPreference(getString(R.string.reset_intake_limit))
            resetWaterIntakeLimitButton?.setOnPreferenceClickListener {
                RateLimiter().setTime(DrinkRecordsController.RATE_LIMIT_KEY, 0, requireContext())
                Toast.makeText(requireContext(), "Drink limit has been reset.", Toast.LENGTH_SHORT).show()
                true
            }

        }
    }
}