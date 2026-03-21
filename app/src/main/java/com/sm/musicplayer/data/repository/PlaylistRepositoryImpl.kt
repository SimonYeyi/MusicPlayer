package com.sm.musicplayer.data.repository

import com.sm.musicplayer.data.local.dao.PlaylistDao
import com.sm.musicplayer.data.local.entity.PlaylistEntity
import com.sm.musicplayer.data.local.entity.PlaylistSongCrossRef
import com.sm.musicplayer.data.local.entity.SongEntity
import com.sm.musicplayer.domain.model.Playlist
import com.sm.musicplayer.domain.model.Song
import com.sm.musicplayer.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepositoryImpl @Inject constructor(
    private val playlistDao: PlaylistDao
) : PlaylistRepository {

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getPlaylistWithSongs(playlistId: Long): Flow<Playlist?> {
        return playlistDao.getPlaylistWithSongs(playlistId).map { playlistWithSongs ->
            playlistWithSongs?.let {
                Playlist(
                    id = it.playlist.id,
                    name = it.playlist.name,
                    songCount = it.songs.size,
                    songs = it.songs.map { song -> song.toDomain() },
                    createdAt = it.playlist.createdAt
                )
            }
        }
    }

    override suspend fun createPlaylist(name: String): Long {
        return playlistDao.insertPlaylist(PlaylistEntity(name = name))
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        playlistDao.updatePlaylist(
            PlaylistEntity(
                id = playlist.id,
                name = playlist.name,
                createdAt = playlist.createdAt
            )
        )
    }

    override suspend fun deletePlaylist(playlist: Playlist) {
        playlistDao.deletePlaylist(
            PlaylistEntity(
                id = playlist.id,
                name = playlist.name,
                createdAt = playlist.createdAt
            )
        )
    }

    override suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        playlistDao.addSongToPlaylist(
            PlaylistSongCrossRef(playlistId = playlistId, songId = songId)
        )
    }

    override suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        playlistDao.removeSongFromPlaylistById(playlistId, songId)
    }

    private fun PlaylistEntity.toDomain() = Playlist(
        id = id,
        name = name,
        createdAt = createdAt
    )

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
}
