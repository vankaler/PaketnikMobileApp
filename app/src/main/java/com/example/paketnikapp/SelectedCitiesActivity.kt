package com.example.paketnikapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.paketnikapp.model.City
import com.example.paketnikapp.tsp.GA
import com.example.paketnikapp.tsp.TSP
import com.example.paketnikapp.tsp.TSP.Tour
import com.example.paketnikapp.viewmodel.CitySelectionViewModel
import com.example.paketnikapp.viewmodel.CitySelectionViewModelFactory

class SelectedCitiesActivity : ComponentActivity() {

    private val viewModel: CitySelectionViewModel by viewModels {
        CitySelectionViewModelFactory(applicationContext)
    }


    companion object {
        const val SELECTED_CITIES_KEY = "SELECTED_CITIES"
        private const val TAG = "SelectedCitiesActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val selectedCities =
            intent.getParcelableArrayListExtra<City>(SELECTED_CITIES_KEY) ?: arrayListOf()
        val cityList = selectedCities.toList()

        Log.d(TAG, "Received ${cityList.size} city(ies) via intent.")

        setContent {
            var calculatedPath by remember { mutableStateOf<List<Int>?>(null) }
            val cities by viewModel.cities.collectAsState()

            Scaffold { paddingValues ->
                Surface(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                ) {
                    SelectedCitiesScreen(
                        selectedCities = cityList,
                        count = cityList.size.toString(),
                        onGenerateMatrix = {
                            // **Functionality Disabled - Matrix Already Generated**

                            /*
                            // **Matrix Generation Code - Disabled for Presentation**
                            lifecycleScope.launch(Dispatchers.IO) {
                                Log.d(TAG, "Generate Matrix button tapped.")

                                try {
                                    val (distanceMatrix, timeMatrix) = fetchDistanceAndTimeMatrices(
                                        cities = cityList,
                                        apiKey = "ZeGeneriranaMatrika"
                                    )

                                    val dirPath = filesDir.absolutePath
                                    Log.d(TAG, "Writing .tsp files to: $dirPath")

                                    DistanceMatrixUtils.writeFullMatrixTSP(
                                        filePath = "$dirPath/distance_matrix.tsp",
                                        matrix = distanceMatrix,
                                        cities = cityList,
                                        name = "Distance Matrix",
                                        comment = "Distances in meters, A->B may differ from B->A"
                                    )

                                    DistanceMatrixUtils.writeFullMatrixTSP(
                                        filePath = "$dirPath/time_matrix.tsp",
                                        matrix = timeMatrix,
                                        cities = cityList,
                                        name = "Time Matrix",
                                        comment = "Driving time in seconds, A->B may differ from B->A"
                                    )

                                    Log.i(TAG, "Wrote TSP files to $dirPath")
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error in onGenerateMatrix", e)
                                }
                            }
                            */

                            // **Alternative Action for Presentation**
                            Log.d(TAG, "Generate Matrix button tapped. Functionality is disabled for presentation.")
                        },
                        onCalculatePath = {

                            var mappedCities = ArrayList<City>()

                            var fileName: String = "distance_matrix.tsp"
                            var tsp: TSP = TSP( this ,fileName, 10000)

                            var ga: GA = GA(100, 0.8, 0.1)

                            var tour: Tour = ga.execute(tsp)

                            tour.path.forEach { city ->
                                viewModel.getByIndex(city.index)?.let { cityByIndex ->
                                    mappedCities.add(cityByIndex)
                                }
                            }

                            /*
                            // **Path Calculation Code - Disabled for Presentation**
                            launchPathCalculation(cityList) { path ->
                                calculatedPath = path
                            }
                            */

                            // **Alternative Action for Presentation**
                            Log.d(TAG, "Calculate Path button tapped. Functionality is disabled as solver is removed.")
                        }
                    )
                }
            }
        }
    }


    /*
    private suspend fun fetchDistanceAndTimeMatrices(
        cities: List<City>,
        apiKey: String
    ): Pair<Array<IntArray>, Array<IntArray>> {

        val N = cities.size
        val distanceMatrix = Array(N) { IntArray(N) }
        val timeMatrix = Array(N) { IntArray(N) }

        if (N <= 1) {
            Log.w(TAG, "Too few cities to compute matrix. Returning empty.")
            return distanceMatrix to timeMatrix
        }

        val latLngs = cities.map { "${it.latitude},${it.longitude}" }
        Log.d(TAG, "Preparing to fetch for $N cities: $latLngs")

        // We'll chunk by 10 to avoid exceeding the per-request limit.
        val chunkSize = 10
        val chunks = latLngs.chunked(chunkSize)
        val client = OkHttpClient()

        for ((i, originChunk) in chunks.withIndex()) {
            for ((j, destinationChunk) in chunks.withIndex()) {
                // Build request URL
                val originsParam = originChunk.joinToString("|")
                val destinationsParam = destinationChunk.joinToString("|")
                val url = "https://maps.googleapis.com/maps/api/distancematrix/json" +
                        "?origins=$originsParam" +
                        "&destinations=$destinationsParam" +
                        "&mode=driving" +
                        "&key=$apiKey"

                Log.d(TAG, "Fetching chunk [$i, $j]")
                Log.d(TAG, "Distance Matrix URL: $url")

                try {
                    val request = Request.Builder().url(url).build()
                    val response = client.newCall(request).execute()

                    val code = response.code
                    val bodyString = response.body?.string().orEmpty()
                    response.close()

                    Log.d(TAG, "Response code: $code")
                    Log.d(TAG, "Response body:\n$bodyString")

                    if (code != 200) {
                        Log.e(TAG, "HTTP error code $code for chunk [$i, $j]. Skipping parsing.")
                        continue // or throw an exception if you want to abort
                    }

                    val root = JSONObject(bodyString)
                    val rows = root.optJSONArray("rows") ?: continue

                    // Use chunkSize for offsets, not 25.
                    val rowOffset = i * chunkSize
                    val colOffset = j * chunkSize

                    for (k in originChunk.indices) {
                        val rowObj = rows.optJSONObject(k)
                        if (rowObj == null) {
                            Log.w(TAG, "rowObj is null in chunk [$i, $j], index=$k")
                            continue
                        }
                        val elements = rowObj.optJSONArray("elements")
                        if (elements == null) {
                            Log.w(TAG, "elements is null in chunk [$i, $j], index=$k")
                            continue
                        }

                        for (l in destinationChunk.indices) {
                            val element = elements.optJSONObject(l)
                            if (element == null) {
                                Log.w(TAG, "element is null in chunk [$i, $j], index=$l")
                                continue
                            }

                            val distVal = element.optJSONObject("distance")
                                ?.optInt("value", 0) ?: 0
                            val durVal = element.optJSONObject("duration")
                                ?.optInt("value", 0) ?: 0

                            val bigRow = rowOffset + k
                            val bigCol = colOffset + l

                            // Make sure we don't go out of bounds just in case
                            if (bigRow in distanceMatrix.indices && bigCol in distanceMatrix.indices) {
                                distanceMatrix[bigRow][bigCol] = distVal
                                timeMatrix[bigRow][bigCol] = durVal

                                Log.d(
                                    TAG,
                                    "Chunk [$i,$j]: origin($bigRow) -> dest($bigCol) dist=$distVal, dur=$durVal"
                                )
                            } else {
                                Log.w(
                                    TAG,
                                    "Index out of range: bigRow=$bigRow, bigCol=$bigCol, N=$N"
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching chunk [$i, $j]", e)
                }
            }
        }

        Log.d(TAG, "Completed chunk fetches.")
        return distanceMatrix to timeMatrix
    }
    */
}
