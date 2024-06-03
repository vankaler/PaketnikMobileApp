package com.example.paketnikapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.paketnikapp.apiUtil.ApiUtil
import com.example.paketnikapp.classes.Client
import com.example.paketnikapp.classes.Room
import com.example.paketnikapp.classes.Staff
import com.example.paketnikapp.ui.theme.PaketnikAppTheme
import org.json.JSONArray
import org.json.JSONObject
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

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

    val formater = DateTimeFormatter.ofPattern("dd. MM. yyyy")

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

            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
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
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Red,
                    contentColor = Color.Black
                ) {
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth()
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
            }

            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                textStyle = TextStyle(color = Color.Black),
                singleLine = true
            )

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
                Text("$itemType ID: $itemId", modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                if(itemType == "Client"){
                    var firstName by remember { mutableStateOf(client!!.name) }
                    var lastName by remember { mutableStateOf(client!!.lastname) }
                    var email by remember { mutableStateOf(client!!.email) }
                    TextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("First Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = TextStyle(color = Color.Black),
                        singleLine = true
                    )
                    TextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Last Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = TextStyle(color = Color.Black),
                        singleLine = true
                    )
                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = TextStyle(color = Color.Black),
                        singleLine = true
                    )
                }
                if(itemType == "Room") {
                    var number by remember { mutableStateOf(room!!.number.toString()) }
                    var size by remember { mutableStateOf(room!!.size.toString()) }
                    TextField(
                        value = number,
                        onValueChange = { number = it },
                        label = { Text("Number") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = TextStyle(color = Color.Black),
                        singleLine = true
                    )
                    TextField(
                        value = size,
                        onValueChange = { size = it },
                        label = { Text("Size") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = TextStyle(color = Color.Black),
                        singleLine = true
                    )
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
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = TextStyle(color = Color.Black),
                        singleLine = true
                    )
                    TextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Last Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = TextStyle(color = Color.Black),
                        singleLine = true
                    )
                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = TextStyle(color = Color.Black),
                        singleLine = true
                    )
                    TextField(
                        value = level,
                        onValueChange = { level = it },
                        label = { Text("Level") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = TextStyle(color = Color.Black),
                        singleLine = true
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
    fun displayClient(client: Client) {
        Card(modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(modifier = Modifier.padding(8.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = "Created: ", style = MaterialTheme.typography.body1, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = client.created.format(formater), style = MaterialTheme.typography.body1)
                    }
                }
                Row(modifier = Modifier.padding(8.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = "ID: ", style = MaterialTheme.typography.body1, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = client.id, style = MaterialTheme.typography.body1)
                    }
                }
                Row(modifier = Modifier.padding(8.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = "Name: ", style = MaterialTheme.typography.body1, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = "${client.name} ${client.lastname}", style = MaterialTheme.typography.body1)
                    }
                }
                Row(modifier = Modifier.padding(8.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = "Email: ", style = MaterialTheme.typography.body1, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = client.email, style = MaterialTheme.typography.body1)
                    }
                }
            }
        }
    }

    @Composable
    fun displayRoom(room: Room) {
        Card(modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
        )
        {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(modifier = Modifier.padding(8.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = "Created: ", style = MaterialTheme.typography.body1, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = room.created.format(formater), style = MaterialTheme.typography.body1)
                    }
                }
                Row(modifier = Modifier.padding(8.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = "ID: ", style = MaterialTheme.typography.body1, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = room.id, style = MaterialTheme.typography.body1)
                    }
                }
                Row(modifier = Modifier.padding(8.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = "Number: ", style = MaterialTheme.typography.body1, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = room.number.toString(), style = MaterialTheme.typography.body1)
                    }
                }
                Row(modifier = Modifier.padding(8.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = "Size: ", style = MaterialTheme.typography.body1, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = room.size.toString(), style = MaterialTheme.typography.body1)
                    }
                }
                Row(modifier = Modifier.padding(8.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = "Occupied: ", style = MaterialTheme.typography.body1, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.padding(8.dp)) {
                        Icon(
                            imageVector = if (room.occupied) Icons.Default.Check else Icons.Default.Clear,
                            contentDescription = if (room.occupied) "Occupied" else "Not occupied"
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun displayStaff(staff: Staff) {
        Card(modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(modifier = Modifier.padding(8.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = "Created: ", style = MaterialTheme.typography.body1, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = staff.created.format(formater), style = MaterialTheme.typography.body1)
                    }
                }
                Row(modifier = Modifier.padding(8.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = "ID: ", style = MaterialTheme.typography.body1, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = staff.id, style = MaterialTheme.typography.body1)
                    }
                }
                Row(modifier = Modifier.padding(8.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = "Name: ", style = MaterialTheme.typography.body1, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = "${staff.name} ${staff.lastname}", style = MaterialTheme.typography.body1)
                    }
                }
                Row(modifier = Modifier.padding(8.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = "Email: ", style = MaterialTheme.typography.body1, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = staff.email, style = MaterialTheme.typography.body1)
                    }
                }
                Row(modifier = Modifier.padding(8.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = "Level: ", style = MaterialTheme.typography.body1, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = staff.level.toString(), style = MaterialTheme.typography.body1)
                    }
                }
            }
        }
    }
}