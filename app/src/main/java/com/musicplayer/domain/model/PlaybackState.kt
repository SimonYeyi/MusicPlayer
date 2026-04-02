package com.musicplayer.domain.model

data class PlaybackState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val playMode: PlayMode = PlayMode.LIST_LOOP
)

enum class PlayMode(val displayName: String) {
    /** 关闭（播放完停止） */
    OFF("关闭"),
    /** 列表循环 */
    LIST_LOOP("列表循环"),
    /** 随机播放 */
    SHUFFLE("随机播放"),
    /** 单曲循环 */
    ONE_LOOP("单曲循环")
}
