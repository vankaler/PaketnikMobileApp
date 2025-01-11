package com.example.paketnikapp

import android.util.Log
import com.example.paketnikapp.model.City
import java.io.File

object DistanceMatrixUtils {

    fun writeFullMatrixTSP(
        filePath: String,
        matrix: Array<IntArray>,
        cities: List<City>,
        name: String,
        comment: String
    ) {
        val N = matrix.size
        val sb = StringBuilder()

        sb.appendLine("NAME: $name")
        sb.appendLine("TYPE: TSP")
        sb.appendLine("COMMENT: $comment")
        sb.appendLine("DIMENSION: $N")
        sb.appendLine("EDGE_WEIGHT_TYPE: EXPLICIT")
        sb.appendLine("EDGE_WEIGHT_FORMAT: FULL_MATRIX")
        sb.appendLine("DISPLAY_DATA_TYPE: TWOD_DISPLAY")
        sb.appendLine("EDGE_WEIGHT_SECTION")

        // NxN
        for (i in 0 until N) {
            val row = matrix[i].joinToString(" ")
            sb.appendLine(row)
        }

        sb.appendLine("DISPLAY_DATA_SECTION")
        for ((index, city) in cities.withIndex()) {
            val lat = city.latitude.toDoubleOrNull() ?: 0.0
            val lon = city.longitude.toDoubleOrNull() ?: 0.0
            // Display coordsov
            sb.appendLine("${index + 1}  $lon  $lat")
        }

        // Additional city data can be removed
        sb.appendLine()
        sb.appendLine("; --- Additional City Data ---")
        for ((index, city) in cities.withIndex()) {
            sb.appendLine("; City ${index + 1}: ${city.name}, ${city.address}, " +
                    "${city.locationDescription}, ${city.postOfficeCode}")
        }

        sb.appendLine("EOF")

        File(filePath).writeText(sb.toString())

        val fileName = File(filePath).name
        Log.i("DistanceMatrixUtils", "Wrote TSP file: $filePath ($fileName)")
    }
}
