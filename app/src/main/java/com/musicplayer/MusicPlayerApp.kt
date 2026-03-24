package com.musicplayer

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.musicplayer.presentation.ui.components.AudioAlbumArtDecoder
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MusicPlayerApp : Application(), ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(AudioAlbumArtDecoder.Factory())
            }
            .crossfade(true)
            .build()
    }
}
