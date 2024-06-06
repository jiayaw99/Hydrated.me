package com.jiuntian.hydratedme.ui.water

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class WaterViewModel : ViewModel() {
    private var auth = FirebaseAuth.getInstance()

    private val _totalVolume = MutableLiveData<Int>().apply {
        value = 0
    }
    val totalVolume: MutableLiveData<Int> = _totalVolume
}