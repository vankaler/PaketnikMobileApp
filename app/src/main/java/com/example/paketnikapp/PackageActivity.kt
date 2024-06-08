package com.example.paketnikapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.paketnikapp.apiUtil.ApiUtil
import com.example.paketnikapp.ui.theme.PaketnikAppTheme
import org.json.JSONArray
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class PackageActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val id = sharedPreferences.getString("userId", "")
        val stringId = id ?: ""
        setContent{
            PaketnikAppTheme {
                Scaffold(
                    bottomBar = { ProfileBottomBar() }
                ) { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues)) {
                        ProfileAppSurface {
                            displayPackageLogs(stringId)
                        }
                    }
                }
            }
        }
    }
}

data class PackageLogs(
    val code: Int,
    val openedBy: String,
    val type: Boolean,
    val timestamp: ZonedDateTime
)

@Composable
fun displayPackageLogs(id: String) {
    var logs = remember { mutableStateOf(mutableStateListOf<PackageLogs>()) }
    LaunchedEffect (Unit){
        ApiUtil.getPackageLogById(
            id,
            onSuccess = { responseBody ->
                val jsonArray = JSONArray(responseBody.string())
                Log.e("PackageActivity", "Response: $jsonArray")
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    logs.value.add(
                        PackageLogs(
                            jsonObject.getInt("code"),
                            jsonObject.getString("openedBy"),
                            jsonObject.getBoolean("type"),
                            ZonedDateTime.parse(jsonObject.getString("date"))
                        )
                    )
                }
                Log.e("PackageActivity", "Logs: $logs")
            },
            onFailure = { error ->
                Log.e("PackageActivity", "Error: $error")
            }
        )
    }
    LazyColumn {
        items(logs.value) { log ->
            LogRow(log)
        }
    }
}

@Composable
fun LogRow(log: PackageLogs) {
    val formatter = DateTimeFormatter.ofPattern("dd. MM. yyyy HH.mm")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp), // Change the background color based on the type variable
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Code", color = Color.Gray)
            Text(text = log.code.toString(), color = if (log.type) Color.White else Color.Red)
        }
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Opened By", color = Color.Gray)
            Text(text = log.openedBy, color = if (log.type) Color.White else Color.Red)
        }
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Timestamp", color = Color.Gray)
            Text(text = log.timestamp.format(formatter), color = if (log.type) Color.White else Color.Red)
        }
    }
}