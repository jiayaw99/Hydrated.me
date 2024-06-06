package com.jiuntian.hydratedme.ui.history

import android.annotation.SuppressLint
import android.app.DatePickerDialog.OnDateSetListener
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.color.MaterialColors
import com.jiuntian.hydratedme.R
import com.jiuntian.hydratedme.controller.HistoryController
import com.jiuntian.hydratedme.databinding.FragmentHistoryBinding
import org.honorato.multistatetogglebutton.MultiStateToggleButton
import java.text.DateFormatSymbols
import java.util.*
import java.util.stream.Collectors

class HistoryFragment : Fragment() {

    val TAG = "history"
    val BUTTON_DAILY = 0
    val BUTTON_WEEK = 1
    val BUTTON_MONTH = 2

    private lateinit var historyViewModel: HistoryViewModel
    private var _binding: FragmentHistoryBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private lateinit var barChart: BarChart
    private lateinit var button: MultiStateToggleButton
    private var daysString = arrayOf("") + (1..31).toList()
        .stream().map(Objects::toString).collect(Collectors.toList())
        .toTypedArray()
    private var listOfMonths = arrayOf("") + DateFormatSymbols().shortMonths

    private val calendar = Calendar.getInstance()

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        historyViewModel =
            ViewModelProvider(this).get(HistoryViewModel::class.java)

        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        barChart = root.findViewById(R.id.bar_chart)
        initializeBarChart(barChart)

        button = root.findViewById(R.id.toggle_button) as MultiStateToggleButton
        if(button.value == -1) button.value = 0 // only reset to 0 when first initialize it
        button.setOnValueChangedListener { position -> setupBarChart(position) }
        HistoryController.getData(this, button.value)

        val button_pick_date: Button = root.findViewById(R.id.button_date_picker)
        button_pick_date.text = DateFormatSymbols().getMonths()[calendar.get(Calendar.MONTH)].toString() + " " + calendar.get(Calendar.YEAR).toString()
        button_pick_date.setOnClickListener {

            val dateSlider = MonthYearPickerDialog()
            val listener =
                OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    button_pick_date.text =
                        DateFormatSymbols().getMonths()[monthOfYear - 1].toString() + " " + year.toString()
                    calendar.set(year, monthOfYear - 1, 1)
                    setupBarChart(button.value)
                }
            dateSlider.setListener(listener)
            dateSlider.show(parentFragmentManager, "MonthYearPickerDialog")
        }
        return root
    }


    override fun onResume() {
        super.onResume()
        //setupBarChart(button.value)
    }

    fun setupBarChart(type: Int) {

        val displayMonth = calendar.time.month
        val displayYear = calendar.time.year
        val displayMaxDays = calendar.getActualMaximum((Calendar.DAY_OF_MONTH))

        if (type == BUTTON_DAILY) {
            val map = HistoryController.dailyWaterDrinked[displayYear]?.get(displayMonth)
            val dataset = BarDataSet(
                HistoryController.addBarEntryDaily(map, displayMaxDays),
                "Drink Volume"
            )
            changeBarChart(barChart, dataset, 10, daysString)

        } else if (type == BUTTON_WEEK) {
            val (dataset, formatter) = HistoryController.addBarEntryWeekly(
                displayMaxDays,
                displayYear,
                displayMonth
            )
            val Dataset = BarDataSet(dataset, "Average Drink Volume per day")

            changeBarChart(
                barChart,
                Dataset,
                formatter.size,
                arrayOf("") + formatter.toTypedArray()
            )
        } else if (type == BUTTON_MONTH) {
            val map = HistoryController.monthlyWaterDrinked[displayYear]
            val dataset =
                BarDataSet(
                    HistoryController.addBarEntryMonthly(map, displayYear),
                    "Average Drink Volume per day"
                )
            changeBarChart(barChart, dataset, 12, listOfMonths)
        } else {
            throw Exception("Wrong value $type")
        }
    }

    private fun initializeBarChart(barChart: BarChart) {
        barChart.description.isEnabled = false
        barChart.axisLeft.axisMinimum = 0f
        barChart.axisLeft.setDrawGridLines(false)
        barChart.axisRight.setDrawGridLines(false)
        barChart.xAxis.setDrawGridLines(false)
        barChart.axisRight.isEnabled = false
        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM

        // barChart.getLegend().setEnabled(false)
        barChart.isDragEnabled = false
        barChart.xAxis.axisMinimum = 0f
    }

    private fun changeBarChart(
        barChart: BarChart,
        dataset: BarDataSet,
        length: Int,
        formatter: Array<String>
    ) {
        dataset.color = MaterialColors.getColor(requireContext(), R.attr.colorSecondary, Color.CYAN)

        val data = BarData(dataset)
        data.barWidth = 0.5f
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(formatter)
        barChart.data = data
        if (length != 10) {// Weekly data need all labels and larger bar gap
            barChart.xAxis.spaceMin = 1f
            barChart.xAxis.labelCount = length
            barChart.setVisibleXRangeMaximum(length.toFloat()+2)
            barChart.xAxis.isGranularityEnabled = false
        } else {  // Daily data
            barChart.setVisibleXRangeMaximum(35f)
//            barChart.xAxis.spaceMin = 1f
//            barChart.xAxis.spaceMax = 1f
            barChart.xAxis.granularity = 10f
            barChart.xAxis.isGranularityEnabled = true
        }
//        barChart.xAxis.setCenterAxisLabels(true)

        barChart.animate()
        barChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        HistoryController.clearData()
    }
}