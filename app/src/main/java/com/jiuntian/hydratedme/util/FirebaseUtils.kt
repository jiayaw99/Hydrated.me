package com.jiuntian.hydratedme.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseUtils {
    val firebaseDatabase = FirebaseFirestore.getInstance()
    val uid = FirebaseAuth.getInstance().currentUser!!.uid
    val firebaseUserDocument = firebaseDatabase.collection("users").document(uid)
    val firebaseWaterIntakeRecords = firebaseUserDocument.collection("drink_records")
   // val firebasePetData = firebaseUserDocument.collection("pet_data")
}