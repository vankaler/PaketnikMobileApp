package com.example.paketnikapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.paketnikapp.model.City
import kotlin.math.pow

@Composable
fun SelectedCitiesScreen(
    selectedCities: List<City>,
    count: String,
    onGenerateMatrix: () -> Unit,
    onCalculatePath: (Double, Double, String) -> Unit // Updated to accept parameters
) {
    var cr by remember { mutableStateOf(0.8f) }
    var pm by remember { mutableStateOf(0.1f) }
    var selectedMatrix by remember { mutableStateOf("Time") }

    Column(modifier = Modifier.padding(16.dp)) {

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Izbrana Mesta",
                style = MaterialTheme.typography.titleLarge
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // GA Parameter Inputs
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(text = "Crossover Rate: ${cr.roundTo(2)}")
            Slider(
                value = cr,
                onValueChange = { cr = it },
                valueRange = 0.0f..1.0f,
                steps = 10,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Mutation Rate: ${pm.roundTo(2)}")
            Slider(
                value = pm,
                onValueChange = { pm = it },
                valueRange = 0.0f..1.0f,
                steps = 10,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Matrix Type Selection
            Text(text = "Select Matrix Type:")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedMatrix == "Distance",
                    onClick = { selectedMatrix = "Distance" }
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Distance Matrix")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(
                    selected = selectedMatrix == "Time",
                    onClick = { selectedMatrix = "Time" }
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Time Matrix")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onGenerateMatrix,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .weight(1f)
            ) {
                Text("Generate Matrix")
            }

            Button(
                onClick = {
                    onCalculatePath(cr.toDouble(), pm.toDouble(), selectedMatrix)
                },
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .weight(1f)
            ) {
                Text("Calculate Path")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedCities.isEmpty()) {
            Text(
                text = "Ni izbranih mest.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyColumn {
                items(selectedCities) { city ->
                    SelectedCityItem(city, count)
                }
            }
        }
    }
}

@Composable
fun SelectedCityItem(city: City, count: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = city.name, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = city.address, style = MaterialTheme.typography.bodySmall)
                Text(text = city.locationDescription, style = MaterialTheme.typography.bodySmall)
                Text(text = "Latitude: ${city.latitude}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Longitude: ${city.longitude}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Lockers: ${city.numberOfLockers}", style = MaterialTheme.typography.bodySmall)
                Text(text = "PostCode: ${city.postOfficeCode}", style = MaterialTheme.typography.bodySmall)
                //Text(text = "Count: $count", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

fun Float.roundTo(decimals: Int): Float {
    val factor = 10.0.pow(decimals.toDouble()).toFloat()
    return kotlin.math.round(this * factor) / factor
}