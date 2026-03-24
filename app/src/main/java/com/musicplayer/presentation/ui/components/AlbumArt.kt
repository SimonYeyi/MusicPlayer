package com.musicplayer.presentation.ui.components

import android.content.ContentUris
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import coil.ImageLoader
import coil.decode.DecodeResult
import coil.decode.Decoder
import coil.decode.ImageSource
import coil.fetch.SourceResult
import coil.request.Options

/**
 * 自定义 Coil 解码器，从音频文件元数据中读取专辑封面图。
 * 绕过 content://media/external/audio/albumart content provider 的权限问题。
 */
class AudioAlbumArtDecoder(
    private val imageSource: ImageSource,
    private val options: Options
) : Decoder {

    override suspend fun decode(): DecodeResult? {
        // 从 ImageSource 的 metadata 获取 URI
        val metadata = imageSource.metadata
        val audioUri: Uri = when {
            metadata != null -> {
                // 通过反射获取 Metadata 中的 uri 字段
                try {
                    val uriField = metadata.javaClass.getDeclaredField("uri")
                    uriField.isAccessible = true
                    uriField.get(metadata) as? Uri ?: return null
                } catch (e: Exception) {
                    return null
                }
            }
            else -> return null
        }

        // 只处理音频内容 URI
        val uriStr = audioUri.toString()
        if (!uriStr.contains("external/audio") || !uriStr.contains("/media/")) return null

        val bitmap = extractAlbumArt(audioUri) ?: return null

        return DecodeResult(
            drawable = BitmapDrawable(options.context.resources, bitmap),
            isSampled = false
        )
    }

    private fun extractAlbumArt(audioUri: Uri): Bitmap? {
        // 策略1：从音频文件元数据读取嵌入封面
        extractFromMetadata(audioUri)?.let { return it }

        // 策略2：从专辑 ID 查询封面
        return try {
            val albumId = getAlbumId(audioUri) ?: return null
            val albumArtUri = ContentUris.withAppendedId(
                Uri.parse("content://media/external/audio/albumart"),
                albumId
            )
            options.context.contentResolver.openInputStream(albumArtUri)?.use { input ->
                BitmapFactory.decodeStream(input)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun extractFromMetadata(audioUri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                options.context.contentResolver.openFileDescriptor(audioUri, "r")?.use { pfd ->
                    val retriever = android.media.MediaMetadataRetriever()
                    retriever.setDataSource(pfd.fileDescriptor)
                    val art = retriever.embeddedPicture?.let { bytes ->
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    }
                    retriever.release()
                    art
                }
            } else {
                options.context.contentResolver.query(
                    audioUri,
                    arrayOf(MediaStore.MediaColumns.DATA),
                    null, null, null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val path = cursor.getString(
                            cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                        )
                        if (path != null) {
                            val retriever = android.media.MediaMetadataRetriever()
                            retriever.setDataSource(path)
                            val art = retriever.embeddedPicture?.let { bytes ->
                                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            }
                            retriever.release()
                            art
                        } else null
                    } else null
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun getAlbumId(audioUri: Uri): Long? {
        return try {
            options.context.contentResolver.query(
                audioUri,
                arrayOf(MediaStore.Audio.Media.ALBUM_ID),
                null, null, null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    class Factory : Decoder.Factory {
        override fun create(result: SourceResult, options: Options, imageLoader: ImageLoader): Decoder? {
            return AudioAlbumArtDecoder(result.source, options)
        }
    }
}
