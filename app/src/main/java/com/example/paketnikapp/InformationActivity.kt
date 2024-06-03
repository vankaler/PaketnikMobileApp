package com.example.paketnikapp

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalElevationOverlay
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
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
            CompositionLocalProvider(
                LocalContentAlpha provides ContentAlpha.high,
                LocalContentColor provides Color.Black,
                LocalTextStyle provides TextStyle(),
                LocalElevationOverlay provides null
            ) {
                displayPosts()
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
    Card(modifier = Modifier
        .padding(8.dp)
        .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(modifier = Modifier.padding(8.dp)) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(text = post.title, style = LocalTextStyle.current, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
            }
            if (post.imageUrl != "null") {
                Row(modifier = Modifier.padding(8.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        AsyncImage(
                            model = "http://$serverIP:3001/${post.imageUrl}",
                            contentDescription = "Translated description of what the image contains"
                        )
                    }
                }
            }
            Row(modifier = Modifier
                .padding(8.dp)
                .background(Color.LightGray)
                .fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(text = post.created.format(formatter), style = LocalTextStyle.current)
                }
            }
        }
    }
}