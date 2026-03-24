package com.musicplayer.domain.model

data class PlaybackState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val playMode: PlayMode = PlayMode.LIST_LOOP
)

enum class PlayMode {
    /** 关闭（播放完停止） */
    OFF,
    /** 列表循环 */
    LIST_LOOP,
    /** 随机播放 */
    SHUFFLE,
    /** 单曲循环 */
    ONE_LOOP
}
