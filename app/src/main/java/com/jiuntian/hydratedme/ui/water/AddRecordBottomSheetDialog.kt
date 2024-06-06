package com.jiuntian.hydratedme.ui.water

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jiuntian.hydratedme.R
import com.jiuntian.hydratedme.controller.DrinkRecordsController
import com.jiuntian.hydratedme.controller.PetDataController
import com.jiuntian.hydratedme.databinding.FragmentAddRecordBottomSheetBinding
import com.jiuntian.hydratedme.model.DrinkType
import com.jiuntian.hydratedme.util.Util
import java.sql.Time
import java.util.*

class AddRecordBottomSheetDialog: BottomSheetDialogFragment(), AdapterView.OnItemSelectedListener {
    private var _binding: FragmentAddRecordBottomSheetBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    // default drinkType
    private var drinkType = DrinkType.WATER
    private val types = Arrays.stream(DrinkType.values()).map { it.printableName }.toArray<String>(::arrayOfNulls)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentAddRecordBottomSheetBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Drink type spinner
        val spinner : Spinner = _binding!!.addWaterIntakeType
        val adapter: ArrayAdapter<String> = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, types)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = this

        // References
        val timePicker: TimePicker = _binding!!.addWaterIntakeTimePicker
        val volumeEditText: EditText = _binding!!.addWaterIntakeVolume
        val cupSize : Int = Util.getPreference(requireContext(), requireContext().getString(R.string.cup_size))
        volumeEditText.inputType = InputType.TYPE_CLASS_NUMBER
        volumeEditText.setText(cupSize.toString())

        val submitButton: Button = _binding!!.addWaterIntakeSubmit
        submitButton.setOnClickListener {
            // Drink time
            val datetime = Date()
            val time = Time(timePicker.currentHour, timePicker.currentMinute, 0)
            datetime.hours = time.hours
            datetime.minutes = time.minutes
            datetime.seconds = time.seconds
            // Volume
            val volume = volumeEditText.text.toString().toInt()

            DrinkRecordsController.addDrinkRecord(datetime, volume, drinkType, context)

            this.dismiss()
        }

        return root
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        drinkType = DrinkType.values()[position]
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }
}