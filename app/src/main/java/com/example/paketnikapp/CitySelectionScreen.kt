package com.example.paketnikapp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.paketnikapp.model.City
import com.example.paketnikapp.viewmodel.CitySelectionViewModel

@Composable
fun CitySelectionScreen(
    viewModel: CitySelectionViewModel,
    onExport: () -> Unit
) {
    val cities by viewModel.cities.collectAsState()
    val selectedCities by viewModel.selectedCities.collectAsState()
    val singleSelectedCity by viewModel.singleSelectedCity.collectAsState()

    val allSelected = selectedCities.size == cities.size && cities.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = allSelected,
                    onCheckedChange = {
                        viewModel.toggleSelectAll()
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Select All",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Export button
            Button(
                onClick = onExport,
                enabled = selectedCities.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "Izvozi",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        MultiSelectCityList(
            cities = cities,
            selectedCities = selectedCities,
            onCityToggle = { viewModel.toggleCitySelection(it) }
        )

        // Divider & single-select section
        Spacer(modifier = Modifier.height(24.dp))
        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Izberite eno mesto",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))

        SingleSelectCityList(
            cities = cities,
            selectedCity = singleSelectedCity,
            onCitySelect = { viewModel.selectSingleCity(it) }
        )
    }
}

@Composable
fun MultiSelectCityList(
    cities: List<City>,
    selectedCities: List<City>,
    onCityToggle: (City) -> Unit
) {
    LazyColumn {
        items(cities, key = { it.address }) { city ->
            CityItem(
                city = city,
                isSelected = selectedCities.contains(city),
                onSelect = { onCityToggle(city) },
                selectionType = SelectionType.Checkbox
            )
        }
    }
}

@Composable
fun SingleSelectCityList(
    cities: List<City>,
    selectedCity: City?,
    onCitySelect: (City) -> Unit
) {
    LazyColumn {
        items(cities, key = { it.address }) { city ->
            CityItem(
                city = city,
                isSelected = (selectedCity == city),
                onSelect = { onCitySelect(city) },
                selectionType = SelectionType.RadioButton
            )
        }
    }
}

@Composable
fun CityItem(
    city: City,
    isSelected: Boolean,
    onSelect: () -> Unit,
    selectionType: SelectionType
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = city.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = city.address,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            when (selectionType) {
                SelectionType.Checkbox -> {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onSelect() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            uncheckedColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
                SelectionType.RadioButton -> {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelect() },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary,
                            unselectedColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }
    }
}

enum class SelectionType {
    Checkbox,
    RadioButton
}
