package com.sm.musicplayer.data.repository

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import com.sm.musicplayer.domain.model.Lyrics
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LyricsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun getLyricsForSong(songPath: String, title: String, artist: String): Lyrics? = withContext(Dispatchers.IO) {
        val possiblePaths = listOf(
            songPath.substringBeforeLast(".") + ".lrc",
            songPath.substringBeforeLast(".") + ".txt",
            "${context.getExternalFilesDir(null)?.absolutePath}/lyrics/${sanitizeFileName(title)} - ${sanitizeFileName(artist)}.lrc",
            "${context.getExternalFilesDir(null)?.absolutePath}/lyrics/${sanitizeFileName(title)}.lrc"
        )

        for (path in possiblePaths) {
            val file = File(path)
            if (file.exists()) {
                return@withContext try {
                    val content = file.readText()
                    Lyrics.parse(content)
                } catch (e: Exception) {
                    null
                }
            }
        }

        val embeddedLyrics = getEmbeddedLyrics(songPath)
        if (embeddedLyrics != null) {
            return@withContext embeddedLyrics
        }

        return@withContext null
    }

    private fun getEmbeddedLyrics(songPath: String): Lyrics? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                null
            } else {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(songPath)
                val lyricsContent = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)
                retriever.release()
                lyricsContent?.let { Lyrics.parse(it) }
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[\\\\/:*?\"<>|]"), "_")
    }

    suspend fun saveLyrics(lyrics: Lyrics, title: String, artist: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val lyricsDir = File("${context.getExternalFilesDir(null)?.absolutePath}/lyrics")
            if (!lyricsDir.exists()) {
                lyricsDir.mkdirs()
            }
            val fileName = "${sanitizeFileName(title)} - ${sanitizeFileName(artist)}.lrc"
            val file = File(lyricsDir, fileName)
            val content = buildString {
                appendLine("[ti:${lyrics.title}]")
                appendLine("[ar:${lyrics.artist}]")
                appendLine("[al:${lyrics.album}]")
                lyrics.lines.forEach { line ->
                    val minutes = line.time / 60000
                    val seconds = (line.time % 60000) / 1000
                    val centiseconds = (line.time % 1000) / 10
                    appendLine("[%02d:%02d.%02d]%s".format(minutes, seconds, centiseconds, line.text))
                }
            }
            file.writeText(content)
            true
        } catch (e: Exception) {
            false
        }
    }
}
