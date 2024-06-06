package com.jiuntian.hydratedme.ui.pet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PetViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is pets Fragment"
    }
    val text: LiveData<String> = _text
}