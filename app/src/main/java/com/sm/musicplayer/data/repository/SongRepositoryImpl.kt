package com.sm.musicplayer.data.repository

import com.sm.musicplayer.data.local.dao.SongDao
import com.sm.musicplayer.data.local.entity.SongEntity
import com.sm.musicplayer.domain.model.Song
import com.sm.musicplayer.domain.repository.SongRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongRepositoryImpl @Inject constructor(
    private val songDao: SongDao
) : SongRepository {

    override fun getAllSongs(): Flow<List<Song>> {
        return songDao.getAllSongs().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getFavoriteSongs(): Flow<List<Song>> {
        return songDao.getFavoriteSongs().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getPopularSongs(): Flow<List<Song>> {
        return songDao.getPopularSongs().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getSongById(id: Long): Song? {
        return songDao.getSongById(id)?.toDomain()
    }

    override suspend fun insertSong(song: Song) {
        songDao.insertSong(song.toEntity())
    }

    override suspend fun insertSongs(songs: List<Song>) {
        songDao.insertSongs(songs.map { it.toEntity() })
    }

    override suspend fun updateFavorite(songId: Long, isFavorite: Boolean) {
        songDao.updateFavorite(songId, isFavorite)
    }

    override suspend fun updatePopular(songId: Long, isPopular: Boolean) {
        songDao.updatePopular(songId, isPopular)
    }

    override suspend fun deleteSong(song: Song) {
        songDao.deleteSong(song.toEntity())
    }

    override suspend fun scanLocalMusic(): List<Song> {
        return emptyList()
    }

    private fun SongEntity.toDomain() = Song(
        id = id,
        title = title,
        artist = artist,
        album = album,
        albumId = albumId,
        duration = duration,
        path = path,
        dateAdded = dateAdded,
        isFavorite = isFavorite,
        isPopular = isPopular
    )

    private fun Song.toEntity() = SongEntity(
        id = id,
        title = title,
        artist = artist,
        album = album,
        albumId = albumId,
        duration = duration,
        path = path,
        dateAdded = dateAdded,
        isFavorite = isFavorite,
        isPopular = isPopular
    )
}
