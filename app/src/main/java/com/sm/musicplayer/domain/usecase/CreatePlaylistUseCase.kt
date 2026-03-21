package com.sm.musicplayer.domain.usecase

import com.sm.musicplayer.domain.repository.PlaylistRepository
import javax.inject.Inject

class CreatePlaylistUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {
    suspend operator fun invoke(name: String): Long {
        return repository.createPlaylist(name)
    }
}
