package com.sm.musicplayer.domain.usecase

import com.sm.musicplayer.domain.repository.SongRepository
import javax.inject.Inject

class TogglePopularUseCase @Inject constructor(
    private val repository: SongRepository
) {
    suspend operator fun invoke(songId: Long, isPopular: Boolean) {
        repository.updatePopular(songId, isPopular)
    }
}
