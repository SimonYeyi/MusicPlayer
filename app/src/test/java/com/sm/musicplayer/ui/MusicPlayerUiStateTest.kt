package com.sm.musicplayer.ui

import com.sm.musicplayer.domain.model.Playlist
import com.sm.musicplayer.domain.model.Song
import org.junit.Assert.assertEquals
import org.junit.Test

class MusicPlayerUiStateTest {

    @Test
    fun `MusicPlayerUiState default values are correct`() {
        val state = MusicPlayerUiState()
        
        assertEquals(false, state.isScanning)
        assertEquals(false, state.showMiniPlayer)
        assertEquals(false, state.showFullPlayer)
        assertEquals(false, state.showSettings)
        assertEquals(0, state.currentTab)
        assertEquals(null, state.selectedPlaylist)
        assertEquals(false, state.showAddToPlaylistDialog)
        assertEquals(null, state.selectedSongForPlaylist)
        assertEquals(false, state.showSearch)
        assertEquals(false, state.showPlaylistDetail)
    }

    @Test
    fun `MusicPlayerUiState copy preserves unchanged values`() {
        val original = MusicPlayerUiState(
            isScanning = true,
            currentTab = 2,
            showSettings = true
        )
        
        val modified = original.copy(showSettings = false)
        
        assertEquals(true, modified.isScanning)
        assertEquals(2, modified.currentTab)
        assertEquals(false, modified.showSettings)
    }

    @Test
    fun `MusicPlayerUiState can track selected playlist`() {
        val playlist = Playlist(id = 1L, name = "My Playlist")
        val state = MusicPlayerUiState(
            selectedPlaylist = playlist,
            showPlaylistDetail = true
        )
        
        assertEquals(playlist, state.selectedPlaylist)
        assertEquals(true, state.showPlaylistDetail)
    }

    @Test
    fun `MusicPlayerUiState can track selected song for playlist`() {
        val song = Song(
            id = 1L,
            title = "Test Song",
            artist = "Artist",
            album = "Album",
            albumId = 1L,
            duration = 180000L,
            path = "/path",
            dateAdded = 0L
        )
        
        val state = MusicPlayerUiState(
            selectedSongForPlaylist = song,
            showAddToPlaylistDialog = true
        )
        
        assertEquals(song, state.selectedSongForPlaylist)
        assertEquals(true, state.showAddToPlaylistDialog)
    }

    @Test
    fun `MusicPlayerUiState can track search state`() {
        val state = MusicPlayerUiState(
            showSearch = true
        )
        
        assertEquals(true, state.showSearch)
    }

    @Test
    fun `MusicPlayerUiState can track all player visibility states`() {
        val state = MusicPlayerUiState(
            showMiniPlayer = true,
            showFullPlayer = false
        )
        
        assertEquals(true, state.showMiniPlayer)
        assertEquals(false, state.showFullPlayer)
    }
}