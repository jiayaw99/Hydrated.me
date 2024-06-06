package com.jiuntian.hydratedme.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.preference.Preference
import com.jiuntian.hydratedme.R
import com.jiuntian.hydratedme.controller.PetDataController
import com.jiuntian.hydratedme.model.PetType
import java.util.stream.Collectors

class ChoosePetDialogFragment(var petTypePreference: Preference) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { it ->
            // Use the Builder class for convenient dialog construction
            val inflater: LayoutInflater = this.layoutInflater
            val view: View = inflater.inflate(R.layout.dialog_choose_pet, null)
            val builder = AlertDialog.Builder(it)
            builder.setMessage(R.string.choose_pet_dialog_message)
                .setView(view)
                .setNegativeButton(
                    R.string.cancel
                ) { dialog, _ ->
                    dialog.cancel()
                }

            val petList = PetType.values()
            val buttonIds = (1..2).toList().stream().map { "pet_button_$it" }
                .collect(Collectors.toList())
            for (i in 0..1) {
                val button: ImageButton = view.findViewById(
                    resources.getIdentifier(
                        buttonIds[i],
                        "id",
                        requireActivity().packageName
                    )
                )
                button.setOnClickListener {
                    PetDataController.updatePet(petList[i])
                    petTypePreference.summary = petList[i].printableName
                   // var preferences = context?.findPreferences("preference_pet")
                    dialog?.dismiss()
                }
            }

            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}