package com.musicplayer.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast

object RingtoneHelper {

    enum class RingtoneType(val title: String, val settingName: String) {
        RINGTONE("来电铃声", Settings.System.RINGTONE),
        NOTIFICATION("通知铃声", Settings.System.NOTIFICATION_SOUND),
        ALARM("闹钟铃声", Settings.System.ALARM_ALERT)
    }

    fun canWriteSettings(context: Context): Boolean {
        return Settings.System.canWrite(context)
    }

    fun setRingtone(context: Context, songUri: Uri, type: RingtoneType): Boolean {
        return try {
            if (canWriteSettings(context)) {
                // 直接设置铃声
                val values = ContentValues().apply {
                    put(type.settingName, songUri.toString())
                }
                context.contentResolver.insert(Settings.System.getUriFor(type.settingName), values)?.let {
                    context.contentResolver.notifyChange(it, null)
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun showRingtonePicker(context: Context, type: RingtoneType, songUri: Uri) {
        try {
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, when (type) {
                    RingtoneType.RINGTONE -> RingtoneManager.TYPE_RINGTONE
                    RingtoneType.NOTIFICATION -> RingtoneManager.TYPE_NOTIFICATION
                    RingtoneType.ALARM -> RingtoneManager.TYPE_ALARM
                })
                putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "设为${type.title}")
                putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, songUri)
                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "设置铃声失败", Toast.LENGTH_SHORT).show()
        }
    }

    fun getRingtoneTypeIntent(context: Context, type: RingtoneType): Intent {
        return Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
    }
}