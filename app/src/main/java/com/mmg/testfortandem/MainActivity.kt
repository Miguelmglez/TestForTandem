package com.mmg.testfortandem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.mmg.testfortandem.presentation.community.CommunityScreen
import com.mmg.testfortandem.presentation.theme.TandemTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TandemTheme {
                CommunityScreen()
            }
        }
    }
}