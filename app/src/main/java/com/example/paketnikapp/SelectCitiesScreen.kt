package com.example.paketnikapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.paketnikapp.model.City

@Composable
fun SelectedCitiesScreen(
    selectedCities: List<City>,
    count: String,
    onGenerateMatrix: () -> Unit,
    onCalculatePath: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Izbrana Mesta", style = MaterialTheme.typography.titleLarge)

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onGenerateMatrix) {
                    Text("Generate Matrix")
                }
                Button(onClick = onCalculatePath) {
                    Text("Calculate Path")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (selectedCities.isEmpty()) {
            Text("Ni izbranih mest.")
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
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = city.name, style = MaterialTheme.typography.titleMedium)
                Text(text = city.address)
                Text(text = city.locationDescription)
                Text(text = "Latitude: ${city.latitude}")
                Text(text = "Longitude: ${city.longitude}")
                Text(text = "Lockers: ${city.numberOfLockers}")
                Text(text = "PostCode: ${city.postOfficeCode}")
                Text(text = "Count: $count")
            }
        }
    }
}
