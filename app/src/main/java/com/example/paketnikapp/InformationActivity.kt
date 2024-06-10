package com.example.paketnikapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import coil.compose.AsyncImage
import com.example.paketnikapp.apiUtil.ApiUtil
import com.example.paketnikapp.apiUtil.serverIP
import org.json.JSONArray
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class InformationActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            Log.e("UncaughtException", "Uncaught exception: ", e)
        }

        setContent {
            CompositionLocalProvider{
                Scaffold(
                    bottomBar = { HomeBottomBar() }
                ) { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues)) {
                        InformationAppSurface {
                        }
                    }
                }
            }
        }
    }
}

data class Post(
    val id: String,
    val title: String,
    val content: String,
    val created: ZonedDateTime,
    var imageUrl: String
)

val formatter = DateTimeFormatter.ofPattern("dd. MM. yyyy")

@Composable
fun InformationAppSurface(onScanClick: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        displayPosts()
    }
}

@Composable
fun displayPosts() {
    val posts by remember { mutableStateOf(mutableStateListOf<Post>()) }
    LaunchedEffect (Unit){
        ApiUtil.getAllInfo(
            onSuccess = { responseBody ->
                val jsonArray = JSONArray(responseBody.string())
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val id = jsonObject.getString("_id")
                    val title = jsonObject.getString("title")
                    val text = jsonObject.getString("text")
                    var image = jsonObject.getString("image")
                    val created = ZonedDateTime.parse(jsonObject.getString("created"))
                    posts.add(Post(id, title, text, created, image))
                }
            },
            onFailure = { error ->
                Log.e("Error", "Failed to get posts", error)
            }
        )
    }

    Log.d("Posts", posts.toString())
    LazyColumn {
        items(posts) { post ->
            displayPost(post)
        }
    }
}

@Composable
fun displayPost(post: Post) {
    post.imageUrl = post.imageUrl.replace("\\", "/")

    Log.d("Post", post.toString())
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            elevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(text = "${post.title}", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "${post.content}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = "http://$serverIP:3001/${post.imageUrl}",
                    contentDescription = "Translated description of what the image contains"
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "${post.created.format(formatter)}", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
fun HomeBottomBar() {
    val context = LocalContext.current
    BottomAppBar {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(
                onClick = {},
                enabled = false
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home"
                )
            }

            IconButton(
                onClick = {
                    val sharedPreferences = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                    sharedPreferences.edit().clear().apply()
                    Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()

                    val intent = Intent(context, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Logout"
                )
            }

            IconButton(
                onClick = {
                    val intent = Intent(context, MainActivity::class.java)
                    context.startActivity(intent)
                },
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "QR Scan"
                )
            }

            IconButton(
                onClick = {
                    val intent = Intent(context, ProfileActivity::class.java)
                    context.startActivity(intent)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile"
                )
            }

        }
    }
}