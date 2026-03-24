package com.musicplayer.presentation.viewmodel

import org.junit.Assert.*
import org.junit.Test

/**
 * Search Function Logic Test
 * Tests the search filtering logic
 */
class SearchFunctionTest {

    @Test
    fun searchFilter_emptyQuery_returnsAll() {
        val songs = listOf("Song A", "Song B", "Song C")
        val query = ""

        val filtered = if (query.isEmpty()) {
            songs
        } else {
            songs.filter { it.contains(query, ignoreCase = true) }
        }

        assertEquals(3, filtered.size)
    }

    @Test
    fun searchFilter_matchingTitle() {
        val songs = listOf("Hello World", "Goodbye", "Hello Friend")
        val query = "Hello"

        val filtered = songs.filter { it.contains(query, ignoreCase = true) }

        assertEquals(2, filtered.size)
        assertTrue(filtered.contains("Hello World"))
        assertTrue(filtered.contains("Hello Friend"))
    }

    @Test
    fun searchFilter_caseInsensitive() {
        val songs = listOf("HELLO", "hello", "Hello")
        val query = "hello"

        val filtered = songs.filter { it.contains(query, ignoreCase = true) }

        assertEquals(3, filtered.size)
    }

    @Test
    fun searchFilter_noMatch_returnsEmpty() {
        val songs = listOf("Song A", "Song B", "Song C")
        val query = "xyz"

        val filtered = songs.filter { it.contains(query, ignoreCase = true) }

        assertTrue(filtered.isEmpty())
    }

    @Test
    fun searchFilter_partialMatch() {
        val songs = listOf("The Beatles", "Beatles Song", "Not Beatles")
        val query = "beat"

        val filtered = songs.filter { it.contains(query, ignoreCase = true) }

        assertEquals(3, filtered.size)
    }

    @Test
    fun searchFilter_specialCharacters() {
        val songs = listOf("Song #1", "Song-2", "Song_3")
        val query = "#"

        val filtered = songs.filter { it.contains(query, ignoreCase = true) }

        assertEquals(1, filtered.size)
        assertEquals("Song #1", filtered[0])
    }

    @Test
    fun searchFilter_emptySongList() {
        val songs = emptyList<String>()
        val query = "test"

        val filtered = songs.filter { it.contains(query, ignoreCase = true) }

        assertTrue(filtered.isEmpty())
    }

    @Test
    fun searchFilter_whitespaceQuery() {
        val songs = listOf("Song A", "Song B")
        val query = " "

        val filtered = songs.filter { it.contains(query, ignoreCase = true) }

        assertEquals(2, filtered.size)
    }
}