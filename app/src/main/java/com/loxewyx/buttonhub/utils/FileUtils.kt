package com.loxewyx.buttonhub.utils

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Locale
import java.util.concurrent.TimeUnit

object FileUtils {
    private const val AUDIO_DIR_NAME = "audio"
    private const val PREFS_NAME = "prefs"
    private const val AUDIO_ORDER_KEY = "audio_ord"
    private var mediaPlayer: MediaPlayer? = null

    /**
     * Lists all audio files in the internal audio directory.
     */
    fun listAudioFiles(context: Context): List<File> {
        val audioDir = File(context.filesDir, AUDIO_DIR_NAME)
        if (!audioDir.exists()) {
            audioDir.mkdir()
        }

        val savedOrder = getSavedAudioOrder(context)
        return savedOrder.mapNotNull { fileName ->
            File(audioDir, fileName).takeIf { it.exists() }
        }
    }

    /**
     * Saves the order of audio files to shared preferences.
     */
    fun saveFileOrder(context: Context, fileNames: List<String>) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(AUDIO_ORDER_KEY, fileNames.joinToString(",")).apply()
    }

    /**
     * Gets the saved order of audio files from shared preferences.
     */
    private fun getSavedAudioOrder(context: Context): List<String> {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedOrder = prefs.getString(AUDIO_ORDER_KEY, "")
        return savedOrder?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
    }

    /**
     * Saves an audio file to internal storage.
     */
    fun saveAudioFile(context: Context, inputStream: InputStream, fileName: String) {
        val audioDir = File(context.filesDir, AUDIO_DIR_NAME)
        if (!audioDir.exists()) {
            audioDir.mkdir()
        }

        val file = File(audioDir, fileName)

        FileOutputStream(file).use { output ->
            inputStream.copyTo(output)
        }

        val currentOrder = getSavedAudioOrder(context).toMutableList()
        if (!currentOrder.contains(fileName)) {
            currentOrder.add(fileName)
        }
        saveFileOrder(context, currentOrder)
    }

    /**
     * Plays an audio file using MediaPlayer.
     */
    fun playAudio(file: File) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(file.path)
            prepare()
            start()
        }
    }

    /**
     * Pauses the currently playing audio.
     */
    fun pauseAudio() {
        mediaPlayer?.pause()
    }

    /**
     * Deletes an audio file.
     */
    fun deleteAudioFile(context: Context, file: File): Boolean {
        val deleted = if (file.exists()) file.delete() else false

        if (deleted) {
            val currentOrder = getSavedAudioOrder(context).toMutableList()
            currentOrder.remove(file.name)
            saveFileOrder(context, currentOrder)
        }

        return deleted
    }

    /**
     * Gets the duration of the audio file.
     */
    fun getAudioDuration(file: File): Int {
        val mediaPlayer = MediaPlayer()
        return try {
            mediaPlayer.setDataSource(file.path)
            mediaPlayer.prepare()
            mediaPlayer.duration
        } catch (e: Exception) {
            Log.e("FileUtils", "Error getting audio duration: ${e.message}")
            0
        } finally {
            mediaPlayer.release()
        }
    }

    /**
     * Gets the current playback position in milliseconds.
     * @return the current position in milliseconds.
     */
    fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    /**
     * Resumes the currently paused audio.
     */
    fun resumeAudio() {
        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.start()
            }
        }
    }

    /**
     * Toggles the liked status of a song.
     */
    fun toggleLikeStatus(context: Context, file: File) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isLiked = prefs.getBoolean(file.name, false)
        prefs.edit().putBoolean(file.name, !isLiked).apply()
    }

    /**
     * Checks if a song is liked.
     */
    fun isSongLiked(context: Context, file: File): Boolean {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(file.name, false)
    }

    /**
     * Helper function to format duration in mm:ss format.
     */
    fun formatDuration(durationMs: Int): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs.toLong())
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs.toLong()) % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

}