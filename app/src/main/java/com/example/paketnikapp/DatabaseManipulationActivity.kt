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
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.paketnikapp.apiUtil.ApiUtil
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import com.example.paketnikapp.classes.Client
import com.example.paketnikapp.classes.Room
import com.example.paketnikapp.classes.Staff
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.ui.Alignment

class DatabaseManipulationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PaketnikAppTheme {
                Navigation()
            }
        }
    }

    enum class Screen {
        Home, Detail
    }

    val formatter = DateTimeFormatter.ofPattern("dd. MM. yyyy")

    @Composable
    fun Navigation(){
        val navController = rememberNavController()
        NavHost(navController, startDestination = Screen.Home.name) {
            composable(Screen.Home.name) {
                showDatabaseManipulation(navController)
            }
            composable("${Screen.Detail.name}/{itemId}/{itemType}") { backStackEntry ->
                val itemId = backStackEntry.arguments?.getString("itemId")
                val itemType = backStackEntry.arguments?.getString("itemType")
                DetailScreen(navController, itemId, itemType)
            }
        }
    }

    @Composable
    private fun showDatabaseManipulation(navController: NavController) {
        var clients by remember { mutableStateOf(mutableStateListOf<Client>()) }
        var rooms by remember { mutableStateOf(mutableStateListOf<Room>()) }
        val staff by remember { mutableStateOf(mutableStateListOf<Staff>()) }
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
                        val created = ZonedDateTime.parse(jsonObject.getString("created"))
                        val updated = ZonedDateTime.parse(jsonObject.getString("updated"))
                        clients.add(Client(id, firstName, lastName, email, created, updated))
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
                        val created = ZonedDateTime.parse(jsonObject.getString("created"))
                        val updated = ZonedDateTime.parse(jsonObject.getString("updated"))
                        rooms.add(Room(id, number, size, occupied, created, updated))
                    }
                    Log.d("DatabaseManipulationActivity", "Rooms: $rooms")
                },
                onFailure = { error ->
                    Log.e("DatabaseManipulationActivity", "Failed to fetch rooms", error)
                }
            )
            ApiUtil.getAllStaff(
                onSuccess = { responseBody ->
                    val jsonArray = JSONArray(responseBody.string())
                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val id = jsonObject.getString("_id")
                        val firstName = jsonObject.getString("firstName")
                        val lastName = jsonObject.getString("lastName")
                        val email = jsonObject.getString("email")
                        val created = ZonedDateTime.parse(jsonObject.getString("created"))
                        val updated = ZonedDateTime.parse(jsonObject.getString("updated"))
                        val level = jsonObject.getInt("level")
                        staff.add(Staff(id, firstName, lastName, email, level, created, updated))
                    }
                    Log.d("DatabaseManipulationActivity", "Staff: $staff")
                },
                onFailure = { error ->
                    Log.e("DatabaseManipulationActivity", "Failed to fetch staff", error)
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

            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)) {
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
                    DropdownMenuItem(onClick = {
                        selectedOption = "Staff"
                        expanded = false
                    }) {
                        Text("Staff")
                    }
                }
            }

            displayLabels(selectedOption)

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                when (selectedOption) {
                    "Clients" -> {
                        val filteredClients = clients.filter {
                            it.id.contains(searchQuery, ignoreCase = true) ||
                                    "${it.name} ${it.lastname}".contains(searchQuery, ignoreCase = true) ||
                                    it.email.contains(searchQuery, ignoreCase = true) ||
                                    it.created.toString().contains(searchQuery, ignoreCase = true)
                        }
                        items(filteredClients.size) { index ->
                            Row(modifier = Modifier.clickable {
                                navController.navigate("${Screen.Detail.name}/${filteredClients[index].id}/Client")
                            }) {
                                displayClient(filteredClients[index])
                            }
                        }
                    }
                    "Rooms" -> {
                        val filteredRooms = rooms.filter {
                            it.id.contains(searchQuery, ignoreCase = true) ||
                                    it.number.toString().contains(searchQuery, ignoreCase = true) ||
                                    it.size.toString().contains(searchQuery, ignoreCase = true) ||
                                    it.occupied.toString().contains(searchQuery, ignoreCase = true) ||
                                    it.created.toString().contains(searchQuery, ignoreCase = true)
                        }
                        items(filteredRooms.size) { index ->
                            Row(modifier = Modifier.clickable {
                                navController.navigate("${Screen.Detail.name}/${filteredRooms[index].id}/Room")
                            }) {
                                displayRoom(filteredRooms[index])
                            }
                        }
                    }
                    "Staff" -> {
                        val filteredStaff = staff.filter {
                            it.id.contains(searchQuery, ignoreCase = true) ||
                                    "${it.name} ${it.lastname}".contains(searchQuery, ignoreCase = true) ||
                                    it.email.contains(searchQuery, ignoreCase = true) ||
                                    it.level.toString().contains(searchQuery, ignoreCase = true) ||
                                    it.created.toString().contains(searchQuery, ignoreCase = true)
                        }
                        items(filteredStaff.size) { index ->
                            Row(modifier = Modifier.clickable {
                                navController.navigate("${Screen.Detail.name}/${filteredStaff[index].id}/Staff")
                            }) {
                                displayStaff(filteredStaff[index])
                            }
                        }

                    }
                }
            }
        }
    }

    @Composable
    fun DetailScreen(navController: NavController, itemId: String?, itemType: String?) {
        var client by remember { mutableStateOf<Client?>(null) }
        var staff by remember { mutableStateOf<Staff?>(null) }
        var room by remember { mutableStateOf<Room?>(null) }
        var isLoading by remember { mutableStateOf(true) }

        if(itemType == "Client"){
            LaunchedEffect (itemId) {
                ApiUtil.getClientById(
                    id = itemId!!,
                    onSuccess = { responseBody ->
                        val jsonObject = JSONObject(responseBody.string())
                        val id = jsonObject.getString("_id")
                        val firstName = jsonObject.getString("firstName")
                        val lastName = jsonObject.getString("lastName")
                        val email = jsonObject.getString("email")
                        val created = ZonedDateTime.parse(jsonObject.getString("created"))
                        val updated = ZonedDateTime.parse(jsonObject.getString("updated"))
                        client = Client(id, firstName, lastName, email, created, updated)
                        isLoading = false
                        Log.d("DatabaseManipulationActivity", "Client: $client")
                    },
                    onFailure = { error ->
                        Log.e("DatabaseManipulationActivity", "Failed to fetch client", error)
                        isLoading = false
                    }
                )
            }
        }
        else if(itemType == "Room") {
            LaunchedEffect(itemId) {
                ApiUtil.getRoomById(
                    id = itemId!!,
                    onSuccess = { responseBody ->
                        val jsonObject = JSONObject(responseBody.string())
                        val id = jsonObject.getString("_id")
                        val number = jsonObject.getInt("number")
                        val size = jsonObject.getInt("size")
                        val occupied = jsonObject.getBoolean("occupied")
                        val created = ZonedDateTime.parse(jsonObject.getString("created"))
                        val updated = ZonedDateTime.parse(jsonObject.getString("updated"))
                        room = Room(id, number, size, occupied, created, updated)
                        isLoading = false
                        Log.d("DatabaseManipulationActivity", "Room: $id")
                    },
                    onFailure = { error ->
                        Log.e("DatabaseManipulationActivity", "Failed to fetch room", error)
                        isLoading = false
                    }
                )
            }
        }
        else if(itemType == "Staff") {
            LaunchedEffect (itemId) {
                ApiUtil.getStaffById(
                    id = itemId!!,
                    onSuccess = { responseBody ->
                        val jsonObject = JSONObject(responseBody.string())
                        val id = jsonObject.getString("_id")
                        val firstName = jsonObject.getString("firstName")
                        val lastName = jsonObject.getString("lastName")
                        val email = jsonObject.getString("email")
                        val created = ZonedDateTime.parse(jsonObject.getString("created"))
                        val updated = ZonedDateTime.parse(jsonObject.getString("updated"))
                        val level = jsonObject.getInt("level")
                        staff = Staff(id, firstName, lastName, email, level, created, updated)
                        isLoading = false
                        Log.d("DatabaseManipulationActivity", "Staff: $staff")
                    },
                    onFailure = { error ->
                        Log.e("DatabaseManipulationActivity", "Failed to fetch staff", error)
                        isLoading = false
                    }
                )
            }
        }

        Column {
            TopAppBar(
                title = { Text("Edit the $itemType") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            } else {
                Text("$itemType ID: $itemId", modifier = Modifier.padding(16.dp))
                if(itemType == "Client"){
                    var firstName by remember { mutableStateOf(client!!.name) }
                    var lastName by remember { mutableStateOf(client!!.lastname) }
                    var email by remember { mutableStateOf(client!!.email) }
                    TextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("First Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Last Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if(itemType == "Room") {
                    var number by remember { mutableStateOf(room!!.number.toString()) }
                    var size by remember { mutableStateOf(room!!.size.toString()) }
                    TextField(
                        value = number,
                        onValueChange = { number = it },
                        label = { Text("Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextField(
                        value = size,
                        onValueChange = { size = it },
                        label = { Text("Size") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Occupied: ",
                            modifier = Modifier.padding(8.dp)
                        )
                        Icon(
                            imageVector = if (room!!.occupied) Icons.Default.Check else Icons.Default.Clear,
                            contentDescription = if (room!!.occupied) "Occupied" else "Not occupied",
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
                else if(itemType == "Staff") {
                    var firstName by remember { mutableStateOf(staff!!.name) }
                    var lastName by remember { mutableStateOf(staff!!.lastname) }
                    var email by remember { mutableStateOf(staff!!.email) }
                    var level by remember { mutableStateOf(staff!!.level.toString()) }
                    TextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("First Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Last Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextField(
                        value = level,
                        onValueChange = { level = it },
                        label = { Text("Level") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Button(
                    onClick = { /* TODO: Implement the function to submit the changes to the server */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Submit")
                }
            }
        }
    }

    @Composable
    fun displayLabels(selectedOption: String) {
        Row {
            when (selectedOption) {
                "Clients" -> {
                    Text("Created", modifier = Modifier
                        .padding(8.dp)
                        .weight(1f))
                    Text("ID", modifier = Modifier
                        .padding(8.dp)
                        .weight(1f))
                    Text("Name", modifier = Modifier
                        .padding(8.dp)
                        .weight(1f))
                    Text("Email", modifier = Modifier
                        .padding(8.dp)
                        .weight(1f))
                }
                "Rooms" -> {
                    Text("Created", modifier = Modifier
                        .padding(8.dp)
                        .weight(1f))
                    Text("ID", modifier = Modifier
                        .padding(8.dp)
                        .weight(1f))
                    Text("Number", modifier = Modifier
                        .padding(8.dp)
                        .weight(1f))
                    Text("Size", modifier = Modifier
                        .padding(8.dp)
                        .weight(1f))
                    Text("Occupied", modifier = Modifier
                        .padding(8.dp)
                        .weight(1f))
                }
                "Staff" -> {
                    Text("Created", modifier = Modifier
                        .padding(8.dp)
                        .weight(1f))
                    Text("ID", modifier = Modifier
                        .padding(8.dp)
                        .weight(1f))
                    Text("Name", modifier = Modifier
                        .padding(8.dp)
                        .weight(1f))
                    Text("Email", modifier = Modifier
                        .padding(8.dp)
                        .weight(1f))
                    Text("Level", modifier = Modifier
                        .padding(8.dp)
                        .weight(1f))
                }
            }
        }
    }

    @Composable
    fun displayClient(client: Client) {
        Row {
            Text(
                text = client.created.format(formatter),
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f)
            )
            Text(
                text = client.id,
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f)
            )
            Text(
                text = client.name + " " + client.lastname,
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f)
            )
            Text(
                text = client.email,
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f)
            )
        }
    }

    @Composable
    fun displayRoom(room: Room) {
        Row {
            Text(
                text = room.created.format(formatter),
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f)
            )
            Text(
                text = room.id,
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f)
            )
            Text(
                text = room.number.toString(),
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f)
            )
            Text(
                text = room.size.toString(),
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f)
            )
            Text(
                text = room.occupied.toString(),
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f)
            )
        }
    }

    @Composable
    fun displayStaff(staff: Staff) {
        Row {
            Text(
                text = staff.created.format(formatter),
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f)
            )
            Text(
                text = staff.id,
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f)
            )
            Text(
                text = staff.name + " " + staff.lastname,
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f)
            )
            Text(
                text = staff.email,
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f)
            )
            val levelColor = when (staff.level.toInt()) {
                1 -> Color.Green
                2 -> Color.Yellow
                3 -> Color.Red
                else -> Color.Gray
            }
            Text(
                text = staff.level.toString(),
                color = levelColor,
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f)
            )
        }
    }
}