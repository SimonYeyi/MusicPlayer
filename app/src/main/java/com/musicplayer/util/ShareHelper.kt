package com.musicplayer.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import com.musicplayer.domain.model.Song

object ShareHelper {

    fun shareSong(context: Context, song: Song): Boolean {
        val uri = song.uri ?: return false

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "audio/*"
            putExtra(Intent.EXTRA_TEXT, "分享歌曲：${song.title} - ${song.artist}")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        return try {
            context.startActivity(Intent.createChooser(intent, "分享歌曲"))
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }
}
