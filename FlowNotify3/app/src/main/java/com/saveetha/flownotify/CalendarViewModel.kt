package com.saveetha.flownotify

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CalendarViewModel : ViewModel() {
    private val _eventCreated = MutableLiveData<Boolean>()
    val eventCreated: LiveData<Boolean> = _eventCreated

    fun setEventCreated(created: Boolean) {
        _eventCreated.value = created
    }
}