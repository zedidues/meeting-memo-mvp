package com.example.meetingmemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.meetingmemo.ui.MeetingMemoApp
import com.example.meetingmemo.ui.theme.MeetingMemoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MeetingMemoTheme {
                MeetingMemoApp()
            }
        }
    }
}
