package com.jiuntian.hydratedme.ui.history

import android.app.AlertDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import com.jiuntian.hydratedme.R
import java.util.*


class MonthYearPickerDialog : DialogFragment() {
    private var listener: OnDateSetListener? = null
    fun setListener(listener: OnDateSetListener?) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        // Get the layout inflater
        val inflater: LayoutInflater = requireActivity().getLayoutInflater()
        val cal: Calendar = Calendar.getInstance()
        val datePickerDialog: View = inflater.inflate(R.layout.date_picker, null)
        val monthPicker = datePickerDialog.findViewById(R.id.picker_month) as NumberPicker
        val yearPicker = datePickerDialog.findViewById(R.id.picker_year) as NumberPicker
        monthPicker.minValue = 1
        monthPicker.maxValue = 12
        monthPicker.value = cal.get(Calendar.MONTH)+1
        val year: Int = cal.get(Calendar.YEAR)
        yearPicker.minValue = 2021
        yearPicker.maxValue = MAX_YEAR
        yearPicker.value = year
        builder.setView(datePickerDialog) // Add action buttons
            .setPositiveButton(R.string.OK
            ) { dialog, id ->
                listener!!.onDateSet(
                    null,
                    yearPicker.value,
                    monthPicker.value,
                    0
                )
            }
            .setNegativeButton(R.string.cancel
            ) { dialog, id ->
                dialog.cancel()
            }
        return builder.create()
    }

    companion object {
        private const val MAX_YEAR = 2030
    }
}