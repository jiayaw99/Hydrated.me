package com.jiuntian.hydratedme.controller

import com.github.mikephil.charting.data.BarEntry
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.QuerySnapshot
import com.jiuntian.hydratedme.model.DrinkRecord
import com.jiuntian.hydratedme.ui.history.HistoryFragment
import com.jiuntian.hydratedme.util.FirebaseUtils
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class HistoryController {
    companion object {
        private val TAG = "history"

        val dailyWaterDrinked: MutableMap<Int?, MutableMap<Int?, MutableMap<Int?, Int?>?>?> =
            HashMap()
        val monthlyWaterDrinked: MutableMap<Int?, MutableMap<Int?, Int?>?> = HashMap()

        private var crossYearFormatter = SimpleDateFormat("d MMM yy", Locale.getDefault())
        private var crossMonthFormatter = SimpleDateFormat("d MMM", Locale.getDefault())
        private var crossDayFormatter = SimpleDateFormat("d", Locale.getDefault())

        fun getData(fragment: HistoryFragment, position: Int): Task<QuerySnapshot> {

            var currentDay: Int = 0
            var currentMonth: Int = -1
            var currentYear: Int = 0

            lateinit var record: DrinkRecord
            val cal = Calendar.getInstance()
            cal.add(Calendar.YEAR, -1)
            val targetDate = cal.time
            var prevDay: Int = -1

            val firebaseUtil = FirebaseUtils()

            return firebaseUtil.firebaseWaterIntakeRecords
                .whereGreaterThanOrEqualTo("time", targetDate)
                .orderBy("time")
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        record = document.toObject(DrinkRecord::class.java)

                        // new fix to previous code that has null pointer bug
                        currentDay = record.time.date
                        currentMonth = record.time.month
                        currentYear = record.time.year

                        // create if not exists
                        dailyWaterDrinked[currentYear] = dailyWaterDrinked[currentYear] ?: HashMap()
                        dailyWaterDrinked[currentYear]!![currentMonth] =
                            dailyWaterDrinked[currentYear]!![currentMonth] ?: HashMap()
                        dailyWaterDrinked[currentYear]!![currentMonth]!![currentDay] =
                            dailyWaterDrinked[currentYear]!![currentMonth]!![currentDay] ?: 0

                        monthlyWaterDrinked[currentYear] = monthlyWaterDrinked[currentYear] ?: HashMap()
                        monthlyWaterDrinked[currentYear]!![currentMonth] =
                            monthlyWaterDrinked[currentYear]!![currentMonth] ?: 0

                        // reset to 0
                        if (currentDay != prevDay) {
                            dailyWaterDrinked[currentYear]!![currentMonth]!![currentDay] = 0
                            monthlyWaterDrinked[currentYear]!![currentMonth] = 0
                        }
                        prevDay = currentDay

                        // increment
                        dailyWaterDrinked[currentYear]!![currentMonth]!![currentDay] =
                            dailyWaterDrinked[currentYear]!![currentMonth]!![currentDay]?.plus(
                                record.volume
                            )
                        monthlyWaterDrinked[currentYear]!![currentMonth] =
                            monthlyWaterDrinked[currentYear]!![currentMonth]?.plus((record.volume))
                    }
                    fragment.setupBarChart(position)
                }
        }

        fun addBarEntryDaily(map: MutableMap<Int?, Int?>?, days: Int): ArrayList<BarEntry?>? {
            var count = 1
            val barEntries = ArrayList<BarEntry?>()
            while (count <= days) {
                var temp = map?.get(count)?.toFloat()
                if (temp == null)
                    temp = 0f
                barEntries.add(BarEntry(count.toFloat(), temp))
                count += 1
            }
            return barEntries
        }

        fun addBarEntryWeekly(
            daysTotalInThisMonth: Int, year: Int, currentMonth: Int
        ): Pair<ArrayList<BarEntry?>?, ArrayList<String>> {
            var count = 1f
            val formatter = ArrayList<String>()
            val barEntries = ArrayList<BarEntry?>()
            var calendar: Calendar = Calendar.getInstance()
            calendar.set(year + 1900, currentMonth, daysTotalInThisMonth, 0, 0) // To get last date
            val lastDayofMonth = calendar.time
            calendar.set(year + 1900, currentMonth, 1, 0, 0)
            val firstDayofMonth = calendar.time.day
            if (firstDayofMonth != 0) { // Sunday
                calendar.add(Calendar.DATE, -firstDayofMonth)
                val triple = getWeeklyTotal(calendar)
                calendar = triple.first
                barEntries.add(BarEntry(count, triple.second))
                formatter.add(triple.third)
                count += 1
            }
            while (calendar.time.before(lastDayofMonth) || calendar.time.equals(lastDayofMonth)) {
                val triple = getWeeklyTotal(calendar)
                calendar = triple.first
                barEntries.add(BarEntry(count, triple.second))
                formatter.add(triple.third)
                count += 1
            }
            return Pair(barEntries, formatter)
        }

        private fun getWeeklyTotal(calendar: Calendar): Triple<Calendar, Float, String> {
            var weeklyTotal = 0f
            var year: Int
            var month: Int
            var date: Int
            var dailyWater: Float?
            val startYear = calendar.time.year
            val startMonth = calendar.time.month
            val start = calendar.clone() as Calendar
            for (i in 0..6) {
                year = calendar.time.year
                month = calendar.time.month
                date = calendar.time.date
                dailyWater =
                    HistoryController.dailyWaterDrinked[year]?.get(month)?.get(date)?.toFloat()
                if (dailyWater != null)
                    weeklyTotal += dailyWater
                if (i != 6)
                    calendar.add(Calendar.DATE, 1)
            }

            val endYear = calendar.time.year
            val endMonth = calendar.time.month
            val end = calendar.clone() as Calendar
            val temp: String
            if (endYear != startYear)
                temp =
                    "${crossYearFormatter.format(start.time)}-${crossYearFormatter.format(end.time)}"
            else if (endMonth != startMonth)
                temp =
                    "${crossMonthFormatter.format(start.time)}-${crossMonthFormatter.format(end.time)}"
            else
                temp =
                    "${crossDayFormatter.format(start.time)}-${crossDayFormatter.format(end.time)}"

            calendar.add(Calendar.DATE, 1)
            return Triple(calendar, weeklyTotal / 7, temp)
        }

        fun addBarEntryMonthly(
            map: MutableMap<Int?, Int?>?,
            currentYear: Int
        ): ArrayList<BarEntry?>? {
            var count = 0
            val calendar = Calendar.getInstance()
            calendar.set(currentYear + 1900, count, 1)
            val barEntries = ArrayList<BarEntry?>()
            while (count <= 11) {
                var temp = map?.get(count)?.toFloat()
                if (temp == null)
                    temp = 0f
                else {
                    temp /= calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                }
                barEntries.add(BarEntry((count + 1).toFloat(), temp))
                count += 1
                calendar.add(Calendar.MONTH, 1)
            }
            return barEntries
        }

        fun clearData() {
            dailyWaterDrinked.clear()
            monthlyWaterDrinked.clear()
        }
    }
}