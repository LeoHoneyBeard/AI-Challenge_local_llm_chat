package com.example.ai_challenge_local_llm_chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ai_challenge_local_llm_chat.ui.theme.AIChallenge_local_llm_chatTheme
import com.example.ai_challenge_local_llm_chat.di.AppModule
import com.example.ai_challenge_local_llm_chat.presentation.chat.ChatApp

class MainActivity : ComponentActivity() {
    private lateinit var appModule: AppModule

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appModule = AppModule(applicationContext)
        enableEdgeToEdge()
        setContent {
            AIChallenge_local_llm_chatTheme {
                ChatApp(appModule = appModule)
            }
        }
    }
}
