package com.example.gemini

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gemini.ui.theme.GeminiTheme
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.launch
import java.util.Locale


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeminiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting()
                }
            }
        }
    }
}
var data = mutableStateOf("")
val generativeModel = GenerativeModel(
    modelName = "gemini-pro",
    apiKey = "API Key"
)
@Composable
fun Greeting(modifier: Modifier = Modifier) {
    val coroutineScope = rememberCoroutineScope()
    var text by remember { mutableStateOf("") }
    var text1 by remember { mutableStateOf("") }
    var text2 by remember { mutableStateOf("") }
    var check by remember { mutableStateOf(false) }
    var you by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) } // Add this line
    val keyboardController = LocalSoftwareKeyboardController.current
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = results?.let { it[0] }
            text = spokenText ?: ""
        }
    }
    Box(modifier.fillMaxSize()) {
        Text(text = "BornikGpt",
            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 25.sp),
            modifier= Modifier
                .align(alignment = Alignment.TopCenter)
                .padding(top = 8.dp)
        )
        Text(text = "____________________",modifier= Modifier
            .align(alignment = Alignment.TopCenter)
            .padding(top = 30.dp))
            LazyColumn(
                modifier = Modifier
                    .padding(bottom = 100.dp, top = 50.dp, start = 12.dp, end = 12.dp) ) {
                item {
                    if(you)
                    {
                        Text(text = "You",style = TextStyle(fontSize = 25.sp, color = Color.Green))
                        Text(text = text2,style = TextStyle(fontSize = 20.sp))
                        Spacer(modifier = modifier.padding(5.dp))
                        Text(text = "BornikGpt",style = TextStyle(fontSize = 25.sp,color = Color.Green))
                    }
                    Text(
                        text = data.value,
                        style = TextStyle(fontSize = 20.sp),
                        modifier=Modifier.align(alignment = Alignment.Center)
                    )
                }
            }
        Card(
            modifier
                .align(alignment = Alignment.BottomCenter)
                .fillMaxWidth(), shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Row(modifier.padding(10.dp)) {
                OutlinedTextField(value = text, singleLine = false, label = { Text("Enter your prompt")},onValueChange = { newText -> text = newText },
                    shape=RoundedCornerShape(12.dp), modifier = Modifier
                        .width(280.dp)
                        .weight(4f),
                )
                    Icon(Icons.Filled.Mic,
                        contentDescription = "Microphone",modifier.size(55.dp).weight(.5f).padding(top = 15.dp).clickable {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak Your Prompt")
                            }
                            launcher.launch(intent)
                        })
                Spacer(modifier = modifier.padding(start = 10.dp))
                Button(onClick = {
                    keyboardController?.hide()
                    if(text!="")
                    {
                        data.value=""
                        text1=text
                        you=true
                        text2=text
                        text=""
                        check = true
                        isLoading = true
                    }
                },
                    modifier.size(65.dp).padding(top=2.dp).weight(1f), shape = CircleShape) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Icon"
                    )
                }
            }
        }
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier
                .align(Alignment.Center)
                .size(60.dp))
        }
    }
    if (check) {
        LaunchedEffect(Unit) {
            coroutineScope.launch {
                data.value = generate(text1) ?: ""
                check = false
                isLoading = false
            }
        }
    }
}
suspend fun generate(fetch: String): String? {
    return try {
        val response = generativeModel.generateContent(fetch)
        response.text
    } catch (e: Exception) {
        e.printStackTrace()
        "Error: ${e.message}"
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GeminiTheme {
        Greeting()
    }
}