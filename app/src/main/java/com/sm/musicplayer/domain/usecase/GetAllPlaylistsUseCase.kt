package com.sm.musicplayer.domain.usecase

import com.sm.musicplayer.domain.model.Playlist
import com.sm.musicplayer.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllPlaylistsUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {
    operator fun invoke(): Flow<List<Playlist>> = repository.getAllPlaylists()
}
