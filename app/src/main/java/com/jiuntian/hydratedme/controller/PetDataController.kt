package com.jiuntian.hydratedme.controller

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks.await
import com.google.firebase.firestore.QuerySnapshot
import com.jiuntian.hydratedme.R
import com.jiuntian.hydratedme.model.DrinkRecord
import com.jiuntian.hydratedme.model.PetType
import com.jiuntian.hydratedme.util.FirebaseUtils
import com.jiuntian.hydratedme.util.Util
import java.util.*

import android.os.AsyncTask
import com.google.firebase.firestore.FieldValue
import java.lang.ref.WeakReference

class PetDataController {
    companion object {
        val firebaseUtils = FirebaseUtils()
        //private val firebasePetData = firebaseUtils.firebasePetData

        fun createPetData(hp: Int, exp: Int, petType: PetType) {
            val hashMap = hashMapOf<String, Any>(
                "hp" to hp,
                "exp" to exp,
                "type" to petType
            )
            firebaseUtils.firebaseUserDocument.set(hashMap)
                .addOnSuccessListener {
                    Log.d("test", "firebase add success")
                }
                .addOnFailureListener {
                    Log.e("firebase", "firebase add pet data failed")
                }
        }


        fun updatePet(new_pet: PetType): Task<Void?> {
            val hashMap = hashMapOf<String, Any>(
                "type" to new_pet,
            )
            return firebaseUtils.firebaseUserDocument.update(hashMap)
        }

        fun gainExp(context: Context, hpValue: Int) {
            firebaseUtils.firebaseUserDocument.update(
                "exp", FieldValue.increment(hpValue.toLong())
            )
            updatePetHp(context, hpValue)
        }

        fun loseExp(hpValue: Int) {
            firebaseUtils.firebaseUserDocument.update(
                "exp", FieldValue.increment(-hpValue.toLong())
            )
        }

        fun updatePetHp(context: Context, value: Int) {
            firebaseUtils.firebaseUserDocument.update(
                "hp", value
            )
            GmsController.submitHp(context, value)
        }

    }

    class GetPetHpTask : AsyncTask<Any?, Any?, Any?>() {

        private lateinit var contextReference: WeakReference<Context>
        fun setContext(context: Context) {
            this.contextReference = WeakReference(context)
        }

        override fun doInBackground(vararg params: Any?): Int {
            val context = contextReference.get()
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DATE, -1)
            val last24Hours = calendar.time
            var totalDrinkInLastDay = 0
            val goal = Util.getPreference(context!!, context.getString(R.string.schedule_goal), "2200")

            val task: Task<QuerySnapshot> = firebaseUtils.firebaseWaterIntakeRecords
                .whereGreaterThanOrEqualTo("time", last24Hours)
                .get()

            val documents: QuerySnapshot = await(task)
            for (document in documents) {
                val record = document.toObject(DrinkRecord::class.java)

                totalDrinkInLastDay += record.volume
            }
            var hpValue = (totalDrinkInLastDay.toFloat() / goal.toFloat() * 100).toInt()
            if (hpValue > 100)
                hpValue = 100

            return hpValue
        }
    }
}