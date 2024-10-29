package com.loxewyx.buttonhub.ui.viewmodel

import android.app.Activity
import android.app.AlertDialog
import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.loxewyx.buttonhub.utils.FileUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.loxewyx.buttonhub.R
import java.io.File

enum class ToneType {
    RINGTONE, NOTIFICATION, ALARM
}

class MusicPlayerViewModel(application: Application) : AndroidViewModel(application) {
    var currentlyPlayingIndex = mutableStateOf<Int?>(null)
        private set
    var isPlaying = mutableStateOf(false)
        private set
    var progress = mutableFloatStateOf(0f)
        private set
    var currentPosition = mutableIntStateOf(0)
        private set

    private var totalDuration = 0
    private var songPlayCount = 20
    private var changedTones = 5
    private var mInterstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null

    init {
        loadInterstitialAd()
        loadRewardedAd()
    }

    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            getApplication(),
            getApplication<Application>().getString(R.string.interstitial_ad),
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                }
            })
    }

    private fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            getApplication() as Context,
            getApplication<Application>().getString(R.string.rewarded_ad),
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    rewardedAd = null
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }
            })
    }

    private fun showInterstitialAd(activity: Activity) {
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(activity)
            loadInterstitialAd()
        }
    }

    private fun showRewardedAd(activity: Activity) {
        rewardedAd?.let { ad ->
            ad.show(
                activity,
                OnUserEarnedRewardListener { _ ->
                    changedTones = 0
            })
        }
    }

    private fun startProgressTracking() {
        viewModelScope.launch {
            while (isPlaying.value) {
                delay(16)
                updateProgress()
            }
        }
    }

    fun playAudio(context: Context, file: File, fileIndex: Int) {
        if (isPlaying.value) {
            FileUtils.pauseAudio()
            isPlaying.value = false
        } else {
            if (++songPlayCount >= 20) {
                showInterstitialAd(context as Activity)
                songPlayCount = 0
            } else {
                FileUtils.playAudio(file)
                totalDuration = FileUtils.getAudioDuration(file)
                currentlyPlayingIndex.value = fileIndex
                isPlaying.value = true
                startProgressTracking()

                if (songPlayCount >= 20) {
                    songPlayCount = 0
                    showInterstitialAd(context as Activity)
                }
            }
        }
    }

    fun pauseAudio() {
        FileUtils.pauseAudio()
        isPlaying.value = false
    }

    fun resumeAudio() {
        if (!isPlaying.value) {
            FileUtils.resumeAudio()
            isPlaying.value = true
            startProgressTracking()
        }
    }

    fun removeAudio(context: Context, file: File, onFileDeleted: () -> Unit) {
        AlertDialog.Builder(context).apply {
            setTitle(context.getString(R.string.musicplayerviewmodel_confirm_deletion_title))
            setMessage(context.getString(R.string.musicplayerviewmodel_confirm_deletion_message))
            setPositiveButton(context.getString(R.string.musicplayerviewmodel_delete)) { _, _ ->
                if (isPlaying.value) {
                    pauseAudio()
                }

                FileUtils.deleteAudioFile(context, file)
                isPlaying.value = false
                progress.floatValue = 1f
                currentlyPlayingIndex.value = null

                onFileDeleted()
            }
            setNegativeButton(context.getString(R.string.musicplayerviewmodel_cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            create()
            show()
        }
    }

    fun updateProgress() {
        if (currentlyPlayingIndex.value != null && totalDuration > 0) {
            currentPosition.intValue = FileUtils.getCurrentPosition()
            progress.floatValue = currentPosition.intValue.toFloat() / totalDuration.toFloat()
            if (currentPosition.intValue >= totalDuration) {
                isPlaying.value = false
                progress.floatValue = 1f
                currentlyPlayingIndex.value = null
            }
        }
    }

    private fun requestModifySystemSettingsPermission(
        context: Context,
        onPermissionGranted: () -> Unit
    ) {
        if (Settings.System.canWrite(context)) {
            onPermissionGranted()
        } else {
            AlertDialog.Builder(context).apply {
                setTitle(context.getString(R.string.musicplayerviewmodel_permission_required_title))
                setMessage(context.getString(R.string.musicplayerviewmodel_permission_required_message))
                setPositiveButton(context.getString(R.string.musicplayerviewmodel_grant_permission)) { _, _ ->
                    val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                }
                setNegativeButton(context.getString(R.string.musicplayerviewmodel_cancel)) { dialog, _ ->
                    dialog.dismiss()
                    Toast.makeText(
                        context,
                        context.getString(R.string.musicplayerviewmodel_permission_required_to_set_tones),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                create()
                show()
            }
        }
    }

    fun setTone(context: Context, file: File, toneType: ToneType) {
        if (!file.exists()) {
            Toast.makeText(
                context,
                context.getString(R.string.musicplayerviewmodel_audio_file_not_exist),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        requestModifySystemSettingsPermission(context) {
            if (++changedTones >= 5) {
                showRewardedAd(context as Activity)
                songPlayCount = 0
            }

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.TITLE, file.name)
                put(MediaStore.MediaColumns.MIME_TYPE, "audio/mpeg")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_RINGTONES)
                } else {
                    put(MediaStore.MediaColumns.DATA, file.absolutePath)
                }

                put(MediaStore.Audio.Media.IS_NOTIFICATION, toneType == ToneType.NOTIFICATION)
                put(MediaStore.Audio.Media.IS_RINGTONE, toneType == ToneType.RINGTONE)
                put(MediaStore.Audio.Media.IS_ALARM, toneType == ToneType.ALARM)
            }

            val contentResolver = context.contentResolver
            val newUri =
                contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)

            if (newUri != null) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        contentResolver.openOutputStream(newUri)?.use { outputStream ->
                            file.inputStream().copyTo(outputStream)
                        }
                    }

                    val systemSetting = when (toneType) {
                        ToneType.NOTIFICATION -> Settings.System.NOTIFICATION_SOUND
                        ToneType.RINGTONE -> Settings.System.RINGTONE
                        ToneType.ALARM -> Settings.System.ALARM_ALERT
                    }

                    Settings.System.putString(contentResolver, systemSetting, newUri.toString())
                    Toast.makeText(
                        context,
                        context.getString(
                            R.string.musicplayerviewmodel_set_tone_success,
                            toneType.name.lowercase()
                        ) + " | " + changedTones,
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.musicplayerviewmodel_set_tone_error, e.message),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.musicplayerviewmodel_media_store_failure),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
