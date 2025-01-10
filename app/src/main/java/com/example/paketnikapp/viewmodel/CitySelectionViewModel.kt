package com.example.paketnikapp.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.paketnikapp.model.City
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class CitySelectionViewModel(private val context: Context) : ViewModel() {

    // Seznam mest
    private val _cities = MutableStateFlow<List<City>>(emptyList())
    val cities: StateFlow<List<City>> = _cities

    // Za večkratno izbiro
    private val _selectedCities = MutableStateFlow<List<City>>(emptyList())
    val selectedCities: StateFlow<List<City>> = _selectedCities

    // Za enkratno izbiro
    private val _singleSelectedCity = MutableStateFlow<City?>(null)
    val singleSelectedCity: StateFlow<City?> = _singleSelectedCity

    init {
        loadCitiesFromJson()
    }

    private fun loadCitiesFromJson() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val jsonString = context.assets.open("cities.json")
                    .bufferedReader()
                    .use { it.readText() }
                val citiesList: List<City> = Json.decodeFromString(jsonString)
                _cities.value = citiesList
            } catch (e: Exception) {
                Log.e("CitySelectionViewModel", "Error loading cities", e)
            }
        }
    }
    // add ali remove
    fun toggleCitySelection(city: City) {
        _selectedCities.value = _selectedCities.value.toMutableList().also { selected ->
            if (selected.contains(city)) {
                selected.remove(city)
            } else {
                selected.add(city)
            }
        }
    }

    fun selectSingleCity(city: City) {
        _singleSelectedCity.value = if (_singleSelectedCity.value != city) city else null
    }

    fun selectAllCities() {
        _selectedCities.value = _cities.value.toMutableList()
    }

    fun clearAllCities() {
        _selectedCities.value = emptyList()
    }

    fun toggleSelectAll() {
        if (_selectedCities.value.size < _cities.value.size) {
            selectAllCities()
        } else {
            clearAllCities()
        }
    }

    /**
     * Pridobi seznam izbranih mest (multi-select in single-select).
     */
    suspend fun getSelectedCitiesList(): List<City> {
        val multiSelected = selectedCities.first()
        val singleSelected = singleSelectedCity.first()

        return if (singleSelected != null) {
            (multiSelected + singleSelected).distinct()
        } else {
            multiSelected
        }
    }
}
