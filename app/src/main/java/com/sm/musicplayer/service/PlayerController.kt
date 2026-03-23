package com.sm.musicplayer.service

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.sm.musicplayer.domain.model.Song
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private val _playerState = MutableStateFlow(PlayerControllerState())
    val playerState: StateFlow<PlayerControllerState> = _playerState.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private var currentPlaylist: List<Song> = emptyList()

    fun initialize() {
        val sessionToken = SessionToken(context, ComponentName(context, MusicPlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            mediaController = controllerFuture?.get()
            setupPlayerListener()
        }, MoreExecutors.directExecutor())
    }

    private fun setupPlayerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                _isPlaying.value = playing
                _playerState.value = _playerState.value.copy(isPlaying = playing)
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updateCurrentSong()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                _playerState.value = _playerState.value.copy(playbackState = playbackState)
            }
        })
    }

    fun playSongs(songs: List<Song>, startIndex: Int = 0) {
        currentPlaylist = songs
        _playerState.value = _playerState.value.copy(playlist = songs, currentIndex = startIndex)
        
        val mediaItems = songs.map { song ->
            MediaItem.Builder()
                .setUri(song.path)
                .setMediaId(song.id.toString())
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .setAlbumTitle(song.album)
                        .build()
                )
                .build()
        }
        
        mediaController?.setMediaItems(mediaItems, startIndex, 0L)
        mediaController?.prepare()
        mediaController?.play()
        
        if (songs.isNotEmpty() && startIndex < songs.size) {
            _currentSong.value = songs[startIndex]
        }
    }

    fun play() {
        mediaController?.play()
    }

    fun pause() {
        mediaController?.pause()
    }

    fun playPause() {
        if (_isPlaying.value) pause() else play()
    }

    fun skipToNext() {
        mediaController?.seekToNext()
        updateCurrentSong()
    }

    fun skipToPrevious() {
        mediaController?.seekToPrevious()
        updateCurrentSong()
    }

    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
    }

    fun seekToPercent(percent: Float): Boolean {
        val duration = mediaController?.duration ?: 0L
        if (duration <= 0 || percent < 0f || percent > 1f) {
            return false
        }
        val position = duration * percent.toLong()
        seekTo(position)
        return true
    }

    fun getCurrentPosition(): Long {
        return mediaController?.currentPosition ?: 0L
    }

    fun getDuration(): Long {
        return mediaController?.duration ?: 0L
    }

    private fun updateCurrentSong() {
        val index = mediaController?.currentMediaItemIndex ?: 0
        if (index in currentPlaylist.indices) {
            _currentSong.value = currentPlaylist[index]
            _playerState.value = _playerState.value.copy(currentIndex = index)
        }
    }

    fun release() {
        controllerFuture?.let { MediaController.releaseFuture(it) }
        mediaController = null
        controllerFuture = null
    }
}

data class PlayerControllerState(
    val isPlaying: Boolean = false,
    val playbackState: Int = Player.STATE_IDLE,
    val playlist: List<Song> = emptyList(),
    val currentIndex: Int = -1
)
