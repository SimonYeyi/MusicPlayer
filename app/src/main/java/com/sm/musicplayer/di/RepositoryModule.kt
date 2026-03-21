package com.sm.musicplayer.di

import com.sm.musicplayer.data.repository.PlaylistRepositoryImpl
import com.sm.musicplayer.data.repository.SongRepositoryImpl
import com.sm.musicplayer.domain.repository.PlaylistRepository
import com.sm.musicplayer.domain.repository.SongRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSongRepository(impl: SongRepositoryImpl): SongRepository

    @Binds
    @Singleton
    abstract fun bindPlaylistRepository(impl: PlaylistRepositoryImpl): PlaylistRepository
}
