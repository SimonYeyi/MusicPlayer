package com.sm.musicplayer.domain.usecase

import com.sm.musicplayer.domain.model.Song
import com.sm.musicplayer.domain.repository.SongRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllSongsUseCase @Inject constructor(
    private val repository: SongRepository
) {
    operator fun invoke(): Flow<List<Song>> = repository.getAllSongs()
}
