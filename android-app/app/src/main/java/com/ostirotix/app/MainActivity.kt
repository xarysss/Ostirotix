package com.ostirotix.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import com.ostirotix.app.nav.OstirotixNavGraph
import com.ostirotix.app.ui.theme.OstirotixTheme
import androidx.compose.material3.Surface

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ServiceLocator.init(this)
        setContent {
            OstirotixTheme {
                Surface(
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    OstirotixNavGraph()
                }
            }
        }
    }
}
