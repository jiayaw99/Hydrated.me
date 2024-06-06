package com.jiuntian.hydratedme.controller

import android.content.Context
import android.os.Handler
import android.util.Log
import android.widget.Toast
import com.jiuntian.hydratedme.model.DrinkType
import com.jiuntian.hydratedme.util.FirebaseUtils
import com.jiuntian.hydratedme.util.RateLimiter
import java.util.*
import android.os.Looper
import com.google.android.material.snackbar.BaseTransientBottomBar


class DrinkRecordsController {
    companion object {
        private val firebaseUtils = FirebaseUtils()
        private val firebaseWaterIntakeRecords = firebaseUtils.firebaseWaterIntakeRecords
        private const val RATE_LIMIT_HOUR = 1;
        const val RATE_LIMIT_KEY = "drink";
        private val rateLimiter = RateLimiter((RATE_LIMIT_HOUR*60*1000).toLong()); // 1hr


        fun addDrinkRecord(date: Date, volume: Int, type: DrinkType, context: Context?) {
            val getPetHpTask = PetDataController.GetPetHpTask()
            getPetHpTask.setContext(context!!)
            val hpValue = getPetHpTask.execute().get().toString().toInt()

            val hashMap = hashMapOf<String, Any>(
                "time" to date,
                "volume" to volume,
                "type" to type.name,
                "expGain" to hpValue
            )
            if (rateLimiter.shouldRun(RATE_LIMIT_KEY, context)) {
                firebaseWaterIntakeRecords.add(hashMap)
                    .addOnSuccessListener {
                        Log.d("firebase", "firebase add success")
                        PetDataController.gainExp(context, hpValue)
                        toastAnywhere(context, "Added $volume ml of ${type.printableName}", Toast.LENGTH_SHORT)
                    }
                    .addOnFailureListener {
                        Log.e("firebase", "firebase add intake records failed", it)
                        toastAnywhere(context, "Failed to add water intake", Toast.LENGTH_LONG)
                    }
            } else {
                Log.d("firebase", "add record rate limit")
                toastAnywhere(context, "Drinking too much harm your health, try again an hour later.", Toast.LENGTH_LONG)
            }
        }

        /**
         * Toast anywhere, even out of the UI thread.
         * Toast is not possible outside UI thread for example worker thread.
         * This allow toast outside UI thread like worker thread.
         */
        private fun toastAnywhere(context : Context, toastText: CharSequence, duration : Int){
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                Toast.makeText(context, toastText, duration).show()
            }, 1)

        }
    }



}