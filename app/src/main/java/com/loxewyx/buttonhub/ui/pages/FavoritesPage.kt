package com.loxewyx.buttonhub.ui.pages

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.loxewyx.buttonhub.R
import com.loxewyx.buttonhub.ui.components.ProgressBar
import com.loxewyx.buttonhub.ui.viewmodel.MusicPlayerViewModel
import com.loxewyx.buttonhub.utils.FileUtils

@Composable
fun FavoritesPage(musicPlayerViewModel: MusicPlayerViewModel) {
    val context = LocalContext.current
    val allAudioFiles = FileUtils.listAudioFiles(context)
    val favoriteFiles = remember {
        derivedStateOf { allAudioFiles.filter { FileUtils.isSongLiked(context, it) } }
    }
    val lazyListState = rememberLazyListState()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (favoriteFiles.value.isEmpty()) {
            Text(
                text = stringResource(R.string.favorites_unavailable),
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(favoriteFiles.value, key = { it.name }) { file ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                val actualIndex = allAudioFiles.indexOf(file)
                                val isCurrentSongPlaying =
                                    musicPlayerViewModel.currentlyPlayingIndex.value == actualIndex

                                if (isCurrentSongPlaying) {
                                    if (musicPlayerViewModel.isPlaying.value) {
                                        musicPlayerViewModel.pauseAudio()
                                    } else {
                                        musicPlayerViewModel.resumeAudio()
                                    }
                                } else {
                                    musicPlayerViewModel.pauseAudio()
                                    musicPlayerViewModel.playAudio(context, file, actualIndex)
                                }
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            if (musicPlayerViewModel.currentlyPlayingIndex.value == allAudioFiles.indexOf(
                                    file
                                )
                            ) {
                                ProgressBar(
                                    progress = musicPlayerViewModel.progress.floatValue,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight()
                                        .alpha(0.2f),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = Color.Transparent
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val fileType = when {
                                        file.name.endsWith(".mp3", true) -> "MP3"
                                        file.name.endsWith(".wav", true) -> "WAV"
                                        file.name.endsWith(".aac", true) -> "AAC"
                                        file.name.endsWith(".mp4", true) -> "MP4"
                                        file.name.endsWith(".m4a", true) -> "M4A"
                                        file.name.endsWith(".ogg", true) -> "OGG"
                                        file.name.endsWith(".flac", true) -> "FAC"
                                        else -> "RAW"
                                    }
                                    Text(
                                        text = fileType,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = file.name.substringBeforeLast('.'),
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(bottom = 8.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val totalDuration = FileUtils.getAudioDuration(file)
                                        val currentPosition =
                                            if (musicPlayerViewModel.currentlyPlayingIndex.value == allAudioFiles.indexOf(
                                                    file
                                                )
                                            ) {
                                                musicPlayerViewModel.currentPosition.intValue
                                            } else 0
                                        if (musicPlayerViewModel.currentlyPlayingIndex.value == allAudioFiles.indexOf(
                                                file
                                            )
                                        ) {
                                            Text(
                                                text = "${FileUtils.formatDuration(currentPosition)} - ${
                                                    FileUtils.formatDuration(
                                                        totalDuration
                                                    )
                                                }",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        } else {
                                            Text(
                                                text = FileUtils.formatDuration(totalDuration),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
