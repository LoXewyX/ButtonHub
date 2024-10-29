package com.loxewyx.buttonhub.ui.pages

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import com.loxewyx.buttonhub.R
import com.loxewyx.buttonhub.ui.components.ProgressBar
import com.loxewyx.buttonhub.ui.viewmodel.MusicPlayerViewModel
import com.loxewyx.buttonhub.ui.viewmodel.ToneType
import com.loxewyx.buttonhub.utils.FileUtils
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun SongsPage(musicPlayerViewModel: MusicPlayerViewModel) {
    val context = LocalContext.current
    var audioFiles by remember { mutableStateOf(FileUtils.listAudioFiles(context)) }
    var expandedItemIndex by remember { mutableStateOf<Int?>(null) }
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        audioFiles = audioFiles.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
        val currentlyPlayingIndex = musicPlayerViewModel.currentlyPlayingIndex.value
        if (from.index == currentlyPlayingIndex) {
            musicPlayerViewModel.currentlyPlayingIndex.value = to.index
        } else if (to.index == currentlyPlayingIndex) {
            musicPlayerViewModel.currentlyPlayingIndex.value = from.index
        }

        FileUtils.saveFileOrder(context, audioFiles.map { it.name })
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (audioFiles.isEmpty()) {
            Text(
                text = stringResource(R.string.songs_no_available),
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
                items(audioFiles, key = { it.name }) { file ->
                    val fileIndex = audioFiles.indexOf(file)
                    ReorderableItem(reorderableLazyListState, key = file.name) { isDragging ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    val isCurrentSongPlaying =
                                        musicPlayerViewModel.currentlyPlayingIndex.value == fileIndex
                                    if (isCurrentSongPlaying) {
                                        if (musicPlayerViewModel.isPlaying.value) {
                                            musicPlayerViewModel.pauseAudio()
                                        } else {
                                            musicPlayerViewModel.resumeAudio()
                                        }
                                    } else {
                                        musicPlayerViewModel.pauseAudio()
                                        musicPlayerViewModel.playAudio(
                                            context,
                                            file,
                                            fileIndex
                                        )
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
                                if (musicPlayerViewModel.currentlyPlayingIndex.value == fileIndex) {
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
                                    IconButton(
                                        modifier = Modifier.draggableHandle(
                                            onDragStarted = { },
                                            onDragStopped = { }
                                        ),
                                        onClick = { }
                                    ) {
                                        Icon(
                                            modifier = Modifier.size(24.dp),
                                            painter = painterResource(id = R.drawable.ic_drag_handle),
                                            contentDescription = stringResource(R.string.songs_drag_handle)
                                        )
                                    }

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
                                                if (musicPlayerViewModel.currentlyPlayingIndex.value == fileIndex) {
                                                    musicPlayerViewModel.currentPosition.intValue
                                                } else 0

                                            if (musicPlayerViewModel.currentlyPlayingIndex.value == fileIndex) {
                                                Text(
                                                    text = "${
                                                        FileUtils.formatDuration(
                                                            currentPosition
                                                        )
                                                    } - ${FileUtils.formatDuration(totalDuration)}",
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

                                            Row {
                                                IconButton(onClick = {
                                                    musicPlayerViewModel.removeAudio(
                                                        context,
                                                        file
                                                    ) {
                                                        audioFiles =
                                                            audioFiles.filter { it != file }
                                                    }
                                                }) {
                                                    Image(
                                                        painter = painterResource(id = R.drawable.ic_delete),
                                                        contentDescription = stringResource(R.string.songs_delete),
                                                        modifier = Modifier.size(24.dp),
                                                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                                                            MaterialTheme.colorScheme.onBackground
                                                        )
                                                    )
                                                }

                                                IconButton(onClick = {
                                                    expandedItemIndex = fileIndex
                                                }) {
                                                    Image(
                                                        painter = painterResource(id = R.drawable.ic_bell),
                                                        modifier = Modifier.size(24.dp),
                                                        contentDescription = stringResource(R.string.songs_tone),
                                                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                                                            MaterialTheme.colorScheme.onBackground
                                                        )
                                                    )
                                                }
                                                DropdownMenu(
                                                    expanded = expandedItemIndex == fileIndex,
                                                    onDismissRequest = { expandedItemIndex = null }
                                                ) {
                                                    DropdownMenuItem(
                                                        onClick = {
                                                            musicPlayerViewModel.setTone(
                                                                context,
                                                                file,
                                                                ToneType.NOTIFICATION
                                                            )
                                                            expandedItemIndex = null
                                                        },
                                                        text = { Text(stringResource(R.string.songs_set_as_notification)) }
                                                    )
                                                    DropdownMenuItem(
                                                        onClick = {
                                                            musicPlayerViewModel.setTone(
                                                                context,
                                                                file,
                                                                ToneType.RINGTONE
                                                            )
                                                            expandedItemIndex = null
                                                        },
                                                        text = { Text(stringResource(R.string.songs_set_as_ringtone)) }
                                                    )
                                                    DropdownMenuItem(
                                                        onClick = {
                                                            musicPlayerViewModel.setTone(
                                                                context,
                                                                file,
                                                                ToneType.ALARM
                                                            )
                                                            expandedItemIndex = null
                                                        },
                                                        text = { Text(stringResource(R.string.songs_set_as_alarm)) }
                                                    )
                                                }

                                                val isLiked = remember {
                                                    mutableStateOf(
                                                        FileUtils.isSongLiked(
                                                            context,
                                                            file
                                                        )
                                                    )
                                                }
                                                IconButton(onClick = {
                                                    FileUtils.toggleLikeStatus(context, file)
                                                    isLiked.value = !isLiked.value
                                                }) {
                                                    Image(
                                                        painter = painterResource(
                                                            id = if (isLiked.value) R.drawable.ic_heart_full else R.drawable.ic_heart
                                                        ),
                                                        contentDescription = stringResource(R.string.songs_favorite),
                                                        modifier = Modifier.size(24.dp),
                                                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                                                            MaterialTheme.colorScheme.onBackground
                                                        )
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
    }
}