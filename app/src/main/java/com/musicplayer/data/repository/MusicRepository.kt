package com.musicplayer.data.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import com.musicplayer.data.local.*
import com.musicplayer.domain.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepository @Inject constructor(
    private val contentResolver: ContentResolver,
    private val songDao: SongDao,
    private val playlistDao: PlaylistDao,
    private val favoriteDao: FavoriteDao,
    private val recentPlayDao: RecentPlayDao
) {
    companion object {
        const val USE_MOCK_DATA = false
    }

    fun getAllSongs(): Flow<List<Song>> {
        return songDao.getAllSongs().map { entities ->
            entities.map { it.toSong() }
        }
    }

    suspend fun hasSongs(): Boolean {
        return songDao.getSongCount() > 0
    }

    suspend fun clearAllSongs() {
        songDao.deleteAll()
    }

    fun searchSongs(query: String): Flow<List<Song>> {
        return songDao.searchSongs(query).map { entities ->
            entities.map { it.toSong() }
        }
    }

    suspend fun getSongById(id: Long): Song? {
        return songDao.getSongById(id)?.toSong()
    }

    suspend fun getSongsByIds(ids: List<Long>): List<Song> {
        return ids.mapNotNull { id ->
            songDao.getSongById(id)?.toSong()
        }
    }

    suspend fun scanMusicFromDevice() {
        // 先清空旧数据，确保已删除的歌曲不会残留
        songDao.deleteAll()

        val songs = mutableListOf<SongEntity>()

        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        contentResolver.query(
            collection,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn) ?: "Unknown"
                val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                val album = cursor.getString(albumColumn) ?: "Unknown Album"
                val duration = cursor.getLong(durationColumn)
                val albumId = cursor.getLong(albumIdColumn)

                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                // albumArtUri 存音频文件 URI，让自定义解码器从元数据中提取封面图
                songs.add(
                    SongEntity(
                        id = id,
                        title = title,
                        artist = artist,
                        album = album,
                        duration = duration,
                        uri = contentUri.toString(),
                        albumArtUri = contentUri.toString()
                    )
                )
            }
        }

        songDao.insertAll(songs)
    }

    // ===== 歌单管理 =====
    fun getAllPlaylists(): Flow<List<PlaylistEntity>> = playlistDao.getAllPlaylists()

    suspend fun createPlaylist(name: String): Long {
        return playlistDao.insertPlaylist(PlaylistEntity(name = name))
    }

    suspend fun deletePlaylist(playlistId: Long) {
        playlistDao.deletePlaylistById(playlistId)
    }

    suspend fun renamePlaylist(playlistId: Long, newName: String) {
        playlistDao.updatePlaylistName(playlistId, newName)
    }

    fun getSongIdsInPlaylist(playlistId: Long): Flow<List<Long>> =
        playlistDao.getSongIdsInPlaylist(playlistId)

    suspend fun getSongsForPlaylist(playlistId: Long): List<Song> {
        val songIds = playlistDao.getSongIdsInPlaylist(playlistId).first()
        return songDao.getAllSongs().first().filter { songIds.contains(it.id) }.map { it.toSong() }
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        playlistDao.addSongToPlaylist(PlaylistSongEntity(playlistId = playlistId, songId = songId))
        playlistDao.updateSongCount(playlistId)
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
        playlistDao.updateSongCount(playlistId)
    }

    suspend fun isSongInPlaylist(playlistId: Long, songId: Long): Boolean =
        playlistDao.isSongInPlaylist(playlistId, songId)

    // ===== 收藏管理 =====
    fun getFavoriteSongIds(): Flow<List<Long>> = favoriteDao.getAllFavoriteSongIds()

    suspend fun addFavorite(songId: Long) {
        favoriteDao.addFavorite(FavoriteEntity(songId = songId))
    }

    suspend fun removeFavorite(songId: Long) {
        favoriteDao.removeFavorite(songId)
    }

    suspend fun isFavorite(songId: Long): Boolean = favoriteDao.isFavorite(songId)

    fun isFavoriteFlow(songId: Long): Flow<Boolean> = favoriteDao.isFavoriteFlow(songId)

    suspend fun toggleFavorite(songId: Long) {
        if (isFavorite(songId)) {
            removeFavorite(songId)
        } else {
            addFavorite(songId)
        }
    }

    // ===== 最近播放管理 =====
    fun getRecentPlaySongIds(limit: Int = 50): Flow<List<Long>> =
        recentPlayDao.getRecentPlaySongIds(limit)

    suspend fun addRecentPlay(songId: Long) {
        recentPlayDao.addRecentPlay(RecentPlayEntity(songId = songId))
        recentPlayDao.cleanupOldPlays()
    }

    suspend fun clearRecentPlays() {
        recentPlayDao.clearAll()
    }

    suspend fun removeFromRecentPlays(songId: Long) {
        recentPlayDao.removeFromRecentPlays(songId)
    }

    // ===== 删除歌曲 =====
    suspend fun deleteSong(songId: Long) {
        songDao.deleteSong(songId)
    }

    suspend fun getSongUri(songId: Long): Uri? {
        return songDao.getSongById(songId)?.let { Uri.parse(it.uri) }
    }

    private fun SongEntity.toSong(): Song {
        return Song(
            id = id,
            title = title,
            artist = artist,
            album = album,
            duration = duration,
            uri = Uri.parse(uri),
            albumArtUri = albumArtUri?.let { Uri.parse(it) }
        )
    }
}
