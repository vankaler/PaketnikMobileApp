package com.example.paketnikapp.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CitySelectionViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CitySelectionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CitySelectionViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
