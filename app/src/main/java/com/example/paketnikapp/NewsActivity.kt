package com.example.paketnikapp

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import coil.compose.rememberImagePainter
import com.example.paketnikapp.apiUtil.ApiUtil
import com.example.paketnikapp.ui.theme.PaketnikAppTheme
import okhttp3.*
import org.json.JSONArray

import org.json.JSONObject
import java.io.IOException

data class NewsArticle(val title: String, val description: String, val imageUrl: String?)

class NewsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PaketnikAppTheme {
                Scaffold(
                        topBar = {
                            TopAppBar(title = { Text("News") })
                        }
                ) { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues)) {
                        NewsScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun NewsScreen() {
    var newsArticles by remember { mutableStateOf(listOf<NewsArticle>()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        fetchNews(
                onSuccess = { articles ->
                    newsArticles = articles
                    errorMessage = null
                },
                onError = { error ->
                    errorMessage = error
                }
        )
    }

    Column(modifier = Modifier.padding(16.dp)) {
        if (errorMessage != null) {
            Text(
                    text = errorMessage ?: "An unknown error occurred",
                    color = Color.Red,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
            )
        }

        LazyColumn {
            items(newsArticles) { article ->
                NewsItem(article)
                Divider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Composable
fun NewsItem(article: NewsArticle) {
    Column(modifier = Modifier.fillMaxWidth()) {
        article.imageUrl?.let { imageUrl ->
            Image(
                    painter = rememberImagePainter(data = imageUrl),
                    contentDescription = null,
                    modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(bottom = 8.dp)
            )
        }
        Text(text = article.title, style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = article.description, style = MaterialTheme.typography.body2)
    }
}

fun fetchNews(onSuccess: (List<NewsArticle>) -> Unit, onError: (String) -> Unit) {
    val client = OkHttpClient()
    val request = Request.Builder()
            .url("https://api.yournewsapi.com/v1/articles") // Replace with your actual API endpoint
            .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            Log.e("NewsActivity", "Failed to fetch news: ${e.message}")
            onError("Failed to fetch news: ${e.message}")
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                response.body?.let { responseBody ->
                    val jsonString = responseBody.string()
                    val jsonArray = JSONArray(jsonString)
                    val articles = mutableListOf<NewsArticle>()

                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val title = jsonObject.getString("title")
                        val description = jsonObject.getString("description")
                        val imageUrl = jsonObject.optString("imageUrl", null)
                        articles.add(NewsArticle(title, description, imageUrl))
                    }

                    onSuccess(articles)
                }
            } else {
                Log.e("NewsActivity", "Failed to fetch news: ${response.message}")
                onError("Failed to fetch news: ${response.message}")
            }
        }
    })
}