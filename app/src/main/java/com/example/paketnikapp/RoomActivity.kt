package com.example.paketnikapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.paketnikapp.apiUtil.ApiUtil
import org.json.JSONObject

class RoomActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val id = sharedPreferences.getString("userId", "")
        Log.e("RoomActivityLogger", id.toString())
        val idString = id ?: ""
        setContent{
            CompositionLocalProvider {
                Scaffold(
                    bottomBar = { ProfileBottomBar() }
                ) { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues)) {
                        ProfileAppSurface {
                            displayRoomInfo(idString)
                        }
                    }
                }
            }
        }
    }
}

data class Room(
    val id: String,
    val number: Int,
    val size: Int,
    val type: Int)

data class RoomContract(
    val id: String,
    val room: Room,
    val startDate: String,
    val endDate: String
)

@Composable
fun displayRoomInfo(id: String) {
    var contracts = remember { mutableStateOf(mutableStateListOf<RoomContract>()) }
    LaunchedEffect (Unit) {
        try{
            ApiUtil.getClientHasRoomsById(id,
                onSuccess = { responseBody ->
                    try{
                        val jsonObject = JSONObject(responseBody.string())
                        Log.e("RoomActivityLogger", "Response: $jsonObject")
                        val roomObject = jsonObject.getJSONObject("room")
                        val room = Room(
                            roomObject.getString("_id"),
                            roomObject.getInt("number"),
                            roomObject.getInt("size"),
                            roomObject.getInt("type")
                        )
                        val contractObject = jsonObject.getJSONObject("clientHasRoom")
                        val contract = RoomContract(
                            contractObject.getString("_id"),
                            room,
                            contractObject.getString("contractCreated"),
                            contractObject.getString("contractEnds")
                        )
                        contracts.value.add(contract)
                    } catch(e: Exception){
                        Log.e("RoomActivityLogger", "Error parsing JSON: ${e.toString()}")
                    }
                },
                onFailure = { error ->
                    Log.e("RoomActivityLogger", "Error: $error")
                }
            )
        } catch (e: Exception) {
            Log.e("RoomActivityLogger", "Error: $e")
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        contracts.value.forEach { contract ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(text = "Contract ID: ${contract.id}", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Room ID: ${contract.room.id}", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Room Number: ${contract.room.number}", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Room Size: ${contract.room.size}", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Room Type: ${contract.room.type}", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Contract Start Date: ${contract.startDate}", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Contract End Date: ${contract.endDate}", style = MaterialTheme.typography.titleMedium)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}