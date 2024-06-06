package com.jiuntian.hydratedme.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.jiuntian.hydratedme.R
import com.jiuntian.hydratedme.util.Util

class OptimizeGoalDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val inflator: LayoutInflater = this.layoutInflater
            val view: View = inflator.inflate(R.layout.dialog_optimize_goal, null)
            val builder = AlertDialog.Builder(it)
            builder.setMessage(R.string.optimize_goal_dialog_message)
                .setView(view)
                .setPositiveButton(
                    R.string.optimize
                ) { dialog, _ ->
                    val bodyMassEditText: EditText =
                        view.findViewById(R.id.optimize_goal_dialog_body_mass_input)
                    try {
                        Log.d("setting", "body weight text: ${bodyMassEditText.text}")
                        val bodyMass: Double = bodyMassEditText.text.toString().toDouble()
//                        val bodyMass = 56.5
                        val newGoal = Util.updateIntakeGoal(
                            requireContext(),
                            bodyMass,
                            getString(R.string.schedule_goal)
                        )
                        Toast.makeText(
                            requireContext(),
                            "Updated goal to $newGoal ml",
                            Toast.LENGTH_LONG
                        ).show()

                    } catch (e: NumberFormatException) {
                        Toast.makeText(
                            requireContext(),
                            "Invalid Input. Number only.",
                            Toast.LENGTH_LONG
                        ).show()
                        dialog.dismiss()
                    }
                }
                .setNegativeButton(
                    R.string.cancel
                ) { dialog, _ ->
                    dialog.cancel()
                }
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}