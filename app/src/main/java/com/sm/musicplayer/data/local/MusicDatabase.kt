package com.sm.musicplayer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sm.musicplayer.data.local.dao.PlaylistDao
import com.sm.musicplayer.data.local.dao.SongDao
import com.sm.musicplayer.data.local.entity.PlaylistEntity
import com.sm.musicplayer.data.local.entity.PlaylistSongCrossRef
import com.sm.musicplayer.data.local.entity.SongEntity

@Database(
    entities = [
        SongEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class
    ],
    version = 2,
    exportSchema = false
)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
}
