package com.musicplayer.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentPlayDao {
    @Query("SELECT * FROM recent_plays ORDER BY playedAt DESC LIMIT :limit")
    fun getRecentPlays(limit: Int = 50): Flow<List<RecentPlayEntity>>

    @Query("SELECT songId FROM recent_plays ORDER BY playedAt DESC LIMIT :limit")
    fun getRecentPlaySongIds(limit: Int = 50): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addRecentPlay(recentPlay: RecentPlayEntity)

    @Query("DELETE FROM recent_plays WHERE songId = :songId")
    suspend fun removeFromRecentPlays(songId: Long)

    @Query("DELETE FROM recent_plays WHERE id NOT IN (SELECT id FROM recent_plays ORDER BY playedAt DESC LIMIT :keepCount)")
    suspend fun cleanupOldPlays(keepCount: Int = 100)

    @Query("DELETE FROM recent_plays")
    suspend fun clearAll()
}
