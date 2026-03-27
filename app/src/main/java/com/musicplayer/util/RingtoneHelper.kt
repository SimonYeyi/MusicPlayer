package com.musicplayer.util

import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast

object RingtoneHelper {

    enum class RingtoneType(val title: String, val androidType: Int) {
        RINGTONE("来电铃声", RingtoneManager.TYPE_RINGTONE),
        NOTIFICATION("通知铃声", RingtoneManager.TYPE_NOTIFICATION),
        ALARM("闹钟铃声", RingtoneManager.TYPE_ALARM)
    }

    fun canWriteSettings(context: Context): Boolean {
        return Settings.System.canWrite(context)
    }

    fun getRingtoneTypeIntent(context: Context, type: RingtoneType): Intent {
        return Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
    }

    fun getRingtonePickerIntent(type: RingtoneType, songUri: Uri?): Intent {
        return Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, type.androidType)
            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "设为${type.title}")
            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, songUri)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
        }
    }

    fun setRingtone(context: Context, type: RingtoneType, songUri: Uri?): Boolean {
        if (songUri == null) return false
        return try {
            RingtoneManager.setActualDefaultRingtoneUri(context, type.androidType, songUri)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}