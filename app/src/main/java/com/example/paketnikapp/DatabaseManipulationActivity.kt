package com.example.paketnikapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import com.example.paketnikapp.ui.theme.PaketnikAppTheme
import java.util.UUID
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import kotlin.random.Random
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.example.paketnikapp.apiUtil.ApiUtil
import org.json.JSONArray
import org.json.JSONObject

class DatabaseManipulationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PaketnikAppTheme {
                showDatabaseManipulation()
            }
        }
    }

    data class Client(val id: String, val name: String, val lastname: String, val email: String)
    data class Room(val id: String, val number: Number, val size: Number, val occupied: Boolean)

    @Composable
    private fun showDatabaseManipulation() {
        var clients by remember { mutableStateOf(mutableStateListOf<Client>()) }
        var rooms by remember { mutableStateOf(mutableStateListOf<Room>()) }
        var searchQuery by remember { mutableStateOf("") }
        var expanded by remember { mutableStateOf(false) }
        var selectedOption by remember { mutableStateOf("Clients") }

        LaunchedEffect(Unit) {
            ApiUtil.getAllClients(
                onSuccess = { responseBody ->
                    val jsonArray = JSONArray(responseBody.string())
                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val id = jsonObject.getString("_id")
                        val firstName = jsonObject.getString("firstName")
                        val lastName = jsonObject.getString("lastName")
                        val email = jsonObject.getString("email")
                        clients.add(Client(id, firstName, lastName, email))
                    }
                    Log.d("DatabaseManipulationActivity", "Clients: $clients")
                },
                onFailure = { error ->
                    Log.e("DatabaseManipulationActivity", "Failed to fetch clients", error)
                }
            )
            ApiUtil.getAllRooms(
                onSuccess = { responseBody ->
                    val jsonArray = JSONArray(responseBody.string())
                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val id = jsonObject.getString("_id")
                        val number = jsonObject.getInt("number")
                        val size = jsonObject.getInt("size")
                        val occupied = jsonObject.getBoolean("occupied")
                        rooms.add(Room(id, number, size, occupied))
                    }
                    Log.d("DatabaseManipulationActivity", "Rooms: $rooms")
                },
                onFailure = { error ->
                    Log.e("DatabaseManipulationActivity", "Failed to fetch rooms", error)
                }
            )
        }

        Column {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search") },
                modifier = Modifier.fillMaxWidth()
            )

            Box(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                Text(
                    text = selectedOption,
                    modifier = Modifier
                        .clickable { expanded = true }
                        .padding(16.dp)
                        .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                        .padding(16.dp),
                    color = Color.Black,
                    fontSize = 20.sp
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(onClick = {
                        selectedOption = "Clients"
                        expanded = false
                    }) {
                        Text("Clients")
                    }
                    DropdownMenuItem(onClick = {
                        selectedOption = "Rooms"
                        expanded = false
                    }) {
                        Text("Rooms")
                    }
                }
            }

            displayLabels(selectedOption)

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                when (selectedOption) {
                    "Clients" -> {
                        val filteredClients = clients.filter {
                            it.id.contains(searchQuery, ignoreCase = true) ||
                                    it.name.contains(searchQuery, ignoreCase = true) ||
                                    it.lastname.contains(searchQuery, ignoreCase = true) ||
                                    it.email.contains(searchQuery, ignoreCase = true)
                        }
                        items(filteredClients.size) { index ->
                            displayClient(filteredClients[index])
                        }
                    }
                    "Rooms" -> {
                        val filteredRooms = rooms.filter {
                            it.id.contains(searchQuery, ignoreCase = true) ||
                                    it.number.toString().contains(searchQuery, ignoreCase = true) ||
                                    it.size.toString().contains(searchQuery, ignoreCase = true) ||
                                    it.occupied.toString().contains(searchQuery, ignoreCase = true)
                        }
                        items(filteredRooms.size) { index ->
                            displayRoom(filteredRooms[index])
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun displayLabels(selectedOption: String) {
        Row {
            when (selectedOption) {
                "Clients" -> {
                    Text("ID", modifier = Modifier.padding(8.dp).weight(1f))
                    Text("First name", modifier = Modifier.padding(8.dp).weight(1f))
                    Text("Last name", modifier = Modifier.padding(8.dp).weight(1f))
                    Text("Email", modifier = Modifier.padding(8.dp).weight(1f))
                }
                "Rooms" -> {
                    Text("ID", modifier = Modifier.padding(8.dp).weight(1f))
                    Text("Number", modifier = Modifier.padding(8.dp).weight(1f))
                    Text("Size", modifier = Modifier.padding(8.dp).weight(1f))
                    Text("Occupied", modifier = Modifier.padding(8.dp).weight(1f))
                }
            }
        }
    }

    @Composable
    fun displayClient(client: Client) {
        Row {
            Text(
                text = client.id,
                modifier = Modifier.padding(8.dp).weight(1f)
            )
            Text(
                text = client.name,
                modifier = Modifier.padding(8.dp).weight(1f)
            )
            Text(
                text = client.lastname,
                modifier = Modifier.padding(8.dp).weight(1f)
            )
            Text(
                text = client.email,
                modifier = Modifier.padding(8.dp).weight(1f)
            )
        }
    }

    @Composable
    fun displayRoom(room: Room) {
        Row {
            Text(
                text = room.id,
                modifier = Modifier.padding(8.dp).weight(1f)
            )
            Text(
                text = room.number.toString(),
                modifier = Modifier.padding(8.dp).weight(1f)
            )
            Text(
                text = room.size.toString(),
                modifier = Modifier.padding(8.dp).weight(1f)
            )
            Text(
                text = room.occupied.toString(),
                modifier = Modifier.padding(8.dp).weight(1f)
            )
        }
    }
}