package com.musicplayer.domain.model

import androidx.compose.ui.graphics.Color

enum class MoodTheme(
    val displayName: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val backgroundColor: Color,
    val surfaceColor: Color,
    val onPrimaryColor: Color,
    val description: String
) {
    HAPPY(
        displayName = "喜庆",
        primaryColor = Color(0xFFE53935),
        secondaryColor = Color(0xFFFF7043),
        backgroundColor = Color(0xFFFFF5F5),
        surfaceColor = Color(0xFFFFEBEE),
        onPrimaryColor = Color.White,
        description = "欢乐、庆祝、热情"
    ),
    SAD(
        displayName = "哀伤",
        primaryColor = Color(0xFF9E9E9E),
        secondaryColor = Color(0xFFBDBDBD),
        backgroundColor = Color(0xFFF5F5F5),
        surfaceColor = Color(0xFFFAFAFA),
        onPrimaryColor = Color.White,
        description = "忧郁、沉思、安静"
    ),
    CALM(
        displayName = "平静",
        primaryColor = Color(0xFF1E88E5),
        secondaryColor = Color(0xFF64B5F6),
        backgroundColor = Color(0xFFE3F2FD),
        surfaceColor = Color(0xFFBBDEFB),
        onPrimaryColor = Color.White,
        description = "放松、安宁、和谐"
    ),
    ENERGETIC(
        displayName = "活力",
        primaryColor = Color(0xFFFF9800),
        secondaryColor = Color(0xFFFFB74D),
        backgroundColor = Color(0xFFFFF3E0),
        surfaceColor = Color(0xFFFFE0B2),
        onPrimaryColor = Color.White,
        description = "充满活力、激情、运动"
    ),
    ROMANTIC(
        displayName = "浪漫",
        primaryColor = Color(0xFFE91E63),
        secondaryColor = Color(0xFFF48FB1),
        backgroundColor = Color(0xFFFCE4EC),
        surfaceColor = Color(0xFFF8BBD0),
        onPrimaryColor = Color.White,
        description = "爱情、甜蜜、温柔"
    ),
    MYSTERIOUS(
        displayName = "神秘",
        primaryColor = Color(0xFF7B1FA2),
        secondaryColor = Color(0xFFBA68C8),
        backgroundColor = Color(0xFFF3E5F5),
        surfaceColor = Color(0xFFE1BEE7),
        onPrimaryColor = Color.White,
        description = "深邃、未知、奇妙"
    ),
    NATURAL(
        displayName = "自然",
        primaryColor = Color(0xFF43A047),
        secondaryColor = Color(0xFF81C784),
        backgroundColor = Color(0xFFE8F5E9),
        surfaceColor = Color(0xFFC8E6C9),
        onPrimaryColor = Color.White,
        description = "清新、生机、环保"
    ),
    WARM(
        displayName = "温暖",
        primaryColor = Color(0xFFFFC107),
        secondaryColor = Color(0xFFFFD54F),
        backgroundColor = Color(0xFFFFF8E1),
        surfaceColor = Color(0xFFFFECB3),
        onPrimaryColor = Color.Black,
        description = "温馨、舒适、阳光"
    ),
    COOL(
        displayName = "冷酷",
        primaryColor = Color(0xFF37474F),
        secondaryColor = Color(0xFF78909C),
        backgroundColor = Color(0xFFECEFF1),
        surfaceColor = Color(0xFFCFD8DC),
        onPrimaryColor = Color.White,
        description = "冷静、理性、简约"
    ),
    FRESH(
        displayName = "清新",
        primaryColor = Color(0xFF00ACC1),
        secondaryColor = Color(0xFF4DD0E1),
        backgroundColor = Color(0xFFE0F7FA),
        surfaceColor = Color(0xFFB2EBF2),
        onPrimaryColor = Color.White,
        description = "清爽、干净、青春"
    )
}

fun getAllMoodThemes(): List<MoodTheme> = MoodTheme.entries.toList()
