package com.sm.musicplayer.domain.model

data class LyricLine(
    val time: Long,
    val text: String
)

data class Lyrics(
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val lines: List<LyricLine> = emptyList()
) {
    companion object {
        fun parse(lrcContent: String): Lyrics {
            val lines = mutableListOf<LyricLine>()
            var title = ""
            var artist = ""
            var album = ""

            lrcContent.lines().forEach { line ->
                val trimmed = line.trim()
                when {
                    trimmed.startsWith("[ti:") -> title = trimmed.removePrefix("[ti:").removeSuffix("]")
                    trimmed.startsWith("[ar:") -> artist = trimmed.removePrefix("[ar:").removeSuffix("]")
                    trimmed.startsWith("[al:") -> album = trimmed.removePrefix("[al:").removeSuffix("]")
                    trimmed.matches(Regex("\\[\\d{2}:\\d{2}(\\.\\d{2})?\\].+")) -> {
                        val timeMatch = Regex("\\[(\\d{2}):(\\d{2})(?:\\.(\\d{2}))?\\]").find(trimmed)
                        val textMatch = Regex("\\].+$").find(trimmed)
                        if (timeMatch != null && textMatch != null) {
                            val minutes = timeMatch.groupValues[1].toLongOrNull() ?: 0
                            val seconds = timeMatch.groupValues[2].toLongOrNull() ?: 0
                            val centiseconds = timeMatch.groupValues[3].toLongOrNull() ?: 0
                            val time = minutes * 60000 + seconds * 1000 + centiseconds * 10
                            val text = textMatch.value.removePrefix("]")
                            if (text.isNotBlank()) {
                                lines.add(LyricLine(time, text))
                            }
                        }
                    }
                }
            }

            return Lyrics(
                title = title,
                artist = artist,
                album = album,
                lines = lines.sortedBy { it.time }
            )
        }
    }

    fun getCurrentLineIndex(currentPosition: Long): Int {
        if (lines.isEmpty()) return -1
        var index = -1
        for (i in lines.indices) {
            if (lines[i].time <= currentPosition) {
                index = i
            } else {
                break
            }
        }
        return index
    }

    fun getNextLineTime(currentPosition: Long): Long? {
        return lines.firstOrNull { it.time > currentPosition }?.time
    }
}
