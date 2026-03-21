package com.sm.musicplayer.domain.repository

import com.sm.musicplayer.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface SongRepository {
    fun getAllSongs(): Flow<List<Song>>
    fun getFavoriteSongs(): Flow<List<Song>>
    fun getPopularSongs(): Flow<List<Song>>
    suspend fun getSongById(id: Long): Song?
    suspend fun insertSong(song: Song)
    suspend fun insertSongs(songs: List<Song>)
    suspend fun updateFavorite(songId: Long, isFavorite: Boolean)
    suspend fun updatePopular(songId: Long, isPopular: Boolean)
    suspend fun deleteSong(song: Song)
    suspend fun scanLocalMusic(): List<Song>
}
