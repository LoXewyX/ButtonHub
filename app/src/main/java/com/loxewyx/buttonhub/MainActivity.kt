package com.loxewyx.buttonhub

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.loxewyx.buttonhub.ui.components.BottomNavBar
import com.loxewyx.buttonhub.ui.pages.AboutPage
import com.loxewyx.buttonhub.ui.pages.FavoritesPage
import com.loxewyx.buttonhub.ui.pages.SettingsPage
import com.loxewyx.buttonhub.ui.pages.SongsPage
import com.loxewyx.buttonhub.ui.theme.ButtonHubTheme
import com.loxewyx.buttonhub.ui.viewmodel.MusicPlayerViewModel

class MainActivity : ComponentActivity() {
    private lateinit var musicPlayerViewModel: MusicPlayerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        musicPlayerViewModel = MusicPlayerViewModel(applicationContext as Application)
        setContent {
            ButtonHubTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val musicPlayerViewModel: MusicPlayerViewModel = viewModel()

    Scaffold(
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = "songs", Modifier.padding(innerPadding)) {
            composable("songs") { SongsPage(musicPlayerViewModel) }
            composable("favorites") { FavoritesPage(musicPlayerViewModel) }
            composable("settings") { SettingsPage(navController) }
            composable("about") { AboutPage(navController) }
        }
    }
}
