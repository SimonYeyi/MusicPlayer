package com.sm.musicplayer.domain.usecase

import com.sm.musicplayer.domain.repository.SongRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val repository: SongRepository
) {
    suspend operator fun invoke(songId: Long, isFavorite: Boolean) {
        repository.updateFavorite(songId, isFavorite)
    }
}
