package com.example.paketnikapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.paketnikapp.viewmodel.CitySelectionViewModel
import com.example.paketnikapp.viewmodel.CitySelectionViewModelFactory
import kotlinx.coroutines.launch

class CitySelectionActivity : ComponentActivity() {

    companion object {
        const val SELECTED_CITIES_KEY = "SELECTED_CITIES"
    }

    private val viewModel: CitySelectionViewModel by viewModels {
        CitySelectionViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Scaffold(
                bottomBar = { ProfileBottomBar() }
            ) { paddingValues ->
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    CitySelectionScreen(
                        viewModel = viewModel,
                        onExport = { exportCities() }
                    )
                }
            }
        }
    }

    private fun exportCities() {
        lifecycleScope.launch {
            try {
                val selectedCities = viewModel.getSelectedCitiesList()
                if (selectedCities.isEmpty()) {
                    Toast.makeText(
                        this@CitySelectionActivity,
                        "Nobenega mesta ni izbranega za izvoz.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                val intent = Intent(this@CitySelectionActivity, SelectedCitiesActivity::class.java).apply {
                    putParcelableArrayListExtra(SELECTED_CITIES_KEY, ArrayList(selectedCities))
                }
                startActivity(intent)

            } catch (e: Exception) {
                Log.e("CitySelectionActivity", "Error exporting cities", e)
                Toast.makeText(
                    this@CitySelectionActivity,
                    "Izvoz ni uspel.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
