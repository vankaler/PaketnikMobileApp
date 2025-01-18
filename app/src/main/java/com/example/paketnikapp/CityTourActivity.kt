package com.example.paketnikapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.paketnikapp.model.City
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

class CityTourActivity : ComponentActivity() {

    companion object {
        private const val TAG = "CityTourActivity"
        const val CITY_LIST_KEY = "CITY_LIST"
        private const val OSRM_URL = "http://router.project-osrm.org/route/v1/driving/"
    }

    private var mapView: MapView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(
            applicationContext,
            applicationContext.getSharedPreferences("osmdroid", MODE_PRIVATE)
        )

        val cities = intent.getParcelableArrayListExtra<City>(CITY_LIST_KEY) ?: emptyList()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CityTourScreen(cities)
                }
            }
        }
    }

    @Composable
    fun CityTourScreen(cities: List<City>) {
        var totalDistance by remember { mutableStateOf(0.0) }
        var totalDuration by remember { mutableStateOf(0.0) }

        val numberedIcons = remember { mutableStateMapOf<Int, BitmapDrawable>() }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Skupna dolžina poti: ${String.format("%.2f", totalDistance / 1000)} km",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Skupni čas potovanja: ${formatDuration(totalDuration)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AndroidView(
                factory = { context ->
                    val mv = MapView(context).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(10.0)


                        for ((index, city) in cities.withIndex()) {
                            try {
                                val latitude = city.latitude.toDouble()
                                val longitude = city.longitude.toDouble()
                                val geoPoint = GeoPoint(latitude, longitude)
                                val marker = Marker(this)
                                marker.position = geoPoint
                                marker.title = "${index + 1}. ${city.name}"

                                val icon = numberedIcons.getOrPut(index + 1) {
                                    createNumberedDrawable(context, index + 1)
                                }
                                marker.icon = icon

                                marker.setOnMarkerClickListener { marker, mapView ->
                                    Toast.makeText(context, marker.title, Toast.LENGTH_SHORT).show()
                                    true
                                }

                                overlays.add(marker)
                            } catch (e: NumberFormatException) {
                                Log.e(TAG, "Invalid latitude or longitude for city: ${city.name}", e)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error adding marker for city: ${city.name}", e)
                            }
                        }

                        if (cities.isNotEmpty()) {
                            try {
                                val firstCity = cities.first()
                                val firstGeoPoint = GeoPoint(firstCity.latitude.toDouble(), firstCity.longitude.toDouble())
                                controller.setCenter(firstGeoPoint)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error setting map center", e)
                            }
                        }
                    }
                    mapView = mv

                    if (cities.size >= 2) {
                        CoroutineScope(Dispatchers.IO).launch {
                            for (i in 0 until cities.size - 1) {
                                val origin = cities[i]
                                val destination = cities[i + 1]
                                val route = getRoute(origin, destination)
                                if (route != null) {
                                    withContext(Dispatchers.Main) {
                                        drawRoute(mv, route)
                                    }
                                    totalDistance += route.distance
                                    totalDuration += route.duration
                                }
                            }
                        }
                    }

                    mv
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(600.dp)
            )
        }
    }


    fun createNumberedDrawable(context: Context, number: Int): BitmapDrawable {
        val size = 50
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paintCircle = Paint().apply {
            color = android.graphics.Color.RED
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paintCircle)

        val paintText = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 20f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val yPos = (canvas.height / 2 - (paintText.descent() + paintText.ascent()) / 2)
        canvas.drawText(number.toString(), size / 2f, yPos, paintText)

        return BitmapDrawable(context.resources, bitmap)
    }

    private fun formatDuration(duration: Double): String {
        val totalSeconds = duration.toLong()
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            "${hours}h ${minutes}m"
        } else {
            "${minutes}m ${seconds}s"
        }
    }

    private fun getRoute(origin: City, destination: City): Route? {
        val client = OkHttpClient()
        val url = "${OSRM_URL}${origin.longitude},${origin.latitude};${destination.longitude},${destination.latitude}?overview=full&geometries=geojson"
        val request = Request.Builder().url(url).build()
        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.e(TAG, "Failed to get route: ${response.message}")
                return null
            }
            val responseBody = response.body?.string() ?: return null
            val json = JSONObject(responseBody)
            val routes = json.getJSONArray("routes")
            if (routes.length() == 0) return null
            val route = routes.getJSONObject(0)
            val distance = route.getDouble("distance")
            val duration = route.getDouble("duration")
            val geometry = route.getJSONObject("geometry")
            val coordinates = geometry.getJSONArray("coordinates")
            val geoPoints = mutableListOf<GeoPoint>()
            for (i in 0 until coordinates.length()) {
                val coord = coordinates.getJSONArray(i)
                val lon = coord.getDouble(0)
                val lat = coord.getDouble(1)
                geoPoints.add(GeoPoint(lat, lon))
            }
            return Route(distance, duration, geoPoints)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching route", e)
            return null
        }
    }

    private fun drawRoute(mapView: MapView, route: Route) {
        val polyline = Polyline()
        polyline.setPoints(route.points)
        polyline.color = 0xFF0000FF.toInt()
        polyline.width = 5f
        mapView.overlays.add(polyline)
        mapView.invalidate()
    }

    data class Route(
        val distance: Double,
        val duration: Double,
        val points: List<GeoPoint>
    )

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }
}
