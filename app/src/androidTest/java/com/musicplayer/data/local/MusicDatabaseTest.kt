package com.musicplayer.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Room Database Integration Test
 */
class MusicDatabaseTest {

    private lateinit var database: MusicDatabase
    private lateinit var songDao: SongDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MusicDatabase::class.java
        ).build()
        songDao = database.songDao()
    }

    @Test
    fun database_isEmpty_initially() = runBlocking {
        val songs = songDao.getAllSongs().first()
        assertTrue(songs.isEmpty())
    }

    @Test
    fun insertSongs_and_retrieveAll() = runBlocking {
        val songs = listOf(
            SongEntity(1L, "Song 1", "Artist 1", "Album 1", 100000L, "uri1", null),
            SongEntity(2L, "Song 2", "Artist 2", "Album 2", 200000L, "uri2", null)
        )

        songDao.insertAll(songs)
        val result = songDao.getAllSongs().first()

        assertEquals(2, result.size)
        assertEquals("Song 1", result[0].title)
        assertEquals("Song 2", result[1].title)
    }

    @Test
    fun getAllSongs_sortedByTitle() = runBlocking {
        val songs = listOf(
            SongEntity(1L, "Zebra", "Artist", "Album", 100000L, "uri1", null),
            SongEntity(2L, "Apple", "Artist", "Album", 100000L, "uri2", null),
            SongEntity(3L, "Banana", "Artist", "Album", 100000L, "uri3", null)
        )

        songDao.insertAll(songs)
        val result = songDao.getAllSongs().first()

        assertEquals(3, result.size)
        assertEquals("Apple", result[0].title)
        assertEquals("Banana", result[1].title)
        assertEquals("Zebra", result[2].title)
    }

    @Test
    fun getSongById_found() = runBlocking {
        val songs = listOf(
            SongEntity(1L, "Song 1", "Artist", "Album", 100000L, "uri1", null),
            SongEntity(2L, "Song 2", "Artist", "Album", 200000L, "uri2", null)
        )

        songDao.insertAll(songs)
        val result = songDao.getSongById(1L)

        assertNotNull(result)
        assertEquals("Song 1", result?.title)
    }

    @Test
    fun getSongById_notFound() = runBlocking {
        val songs = listOf(
            SongEntity(1L, "Song 1", "Artist", "Album", 100000L, "uri1", null)
        )

        songDao.insertAll(songs)
        val result = songDao.getSongById(999L)

        assertNull(result)
    }

    @Test
    fun deleteAll_removesAllSongs() = runBlocking {
        val songs = listOf(
            SongEntity(1L, "Song 1", "Artist", "Album", 100000L, "uri1", null),
            SongEntity(2L, "Song 2", "Artist", "Album", 200000L, "uri2", null)
        )

        songDao.insertAll(songs)
        songDao.deleteAll()

        val result = songDao.getAllSongs().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun searchSongs_byTitle() = runBlocking {
        val songs = listOf(
            SongEntity(1L, "Hello World", "Artist A", "Album", 100000L, "uri1", null),
            SongEntity(2L, "Goodbye", "Artist B", "Album", 100000L, "uri2", null),
            SongEntity(3L, "Hello Friend", "Artist C", "Album", 100000L, "uri3", null)
        )

        songDao.insertAll(songs)
        val result = songDao.searchSongs("Hello").first()

        assertEquals(2, result.size)
    }

    @Test
    fun searchSongs_byArtist() = runBlocking {
        val songs = listOf(
            SongEntity(1L, "Song 1", "John Doe", "Album", 100000L, "uri1", null),
            SongEntity(2L, "Song 2", "Jane Doe", "Album", 100000L, "uri2", null),
            SongEntity(3L, "Song 3", "John Smith", "Album", 100000L, "uri3", null)
        )

        songDao.insertAll(songs)
        val result = songDao.searchSongs("John").first()

        assertEquals(2, result.size)
    }

    @Test
    fun insertAll_replacesExistingSong() = runBlocking {
        val songs = listOf(
            SongEntity(1L, "Original", "Artist", "Album", 100000L, "uri1", null)
        )

        songDao.insertAll(songs)

        val updated = listOf(
            SongEntity(1L, "Updated", "Artist", "Album", 100000L, "uri1", null)
        )
        songDao.insertAll(updated)

        val result = songDao.getAllSongs().first()
        assertEquals(1, result.size)
        assertEquals("Updated", result[0].title)
    }
}
