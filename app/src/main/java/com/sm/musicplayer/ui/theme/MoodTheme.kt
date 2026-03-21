package com.sm.musicplayer.ui.theme

import androidx.compose.ui.graphics.Color

// 10种心情主题色彩方案

// 1. 热情洋溢 (热情红)
object PassionRed {
    val Primary = Color(0xFFE94560)
    val Secondary = Color(0xFFFF6B6B)
    val Background = Color(0xFF1A0A0A)
    val Surface = Color(0xFF2D1B1B)
    val OnPrimary = Color(0xFFFFFFFF)
    val OnBackground = Color(0xFFFFFFFF)
    val OnSurface = Color(0xFFFFE4E4)
    val Accent = Color(0xFFFF4757)
}

// 2. 沉默哀伤 (忧郁灰)
object MelancholyGray {
    val Primary = Color(0xFF6B7280)
    val Secondary = Color(0xFF9CA3AF)
    val Background = Color(0xFF1F2937)
    val Surface = Color(0xFF374151)
    val OnPrimary = Color(0xFFFFFFFF)
    val OnBackground = Color(0xFFE5E7EB)
    val OnSurface = Color(0xFFD1D5DB)
    val Accent = Color(0xFF4B5563)
}

// 3. 平静宁和 (清新蓝)
object CalmBlue {
    val Primary = Color(0xFF3B82F6)
    val Secondary = Color(0xFF60A5FA)
    val Background = Color(0xFF0F172A)
    val Surface = Color(0xFF1E293B)
    val OnPrimary = Color(0xFFFFFFFF)
    val OnBackground = Color(0xFFFFFFFF)
    val OnSurface = Color(0xFFBFDBFE)
    val Accent = Color(0xFF2563EB)
}

// 4. 活力阳光 (明媚黄)
object SunnyYellow {
    val Primary = Color(0xFFFBBF24)
    val Secondary = Color(0xFFFCD34D)
    val Background = Color(0xFF1C1400)
    val Surface = Color(0xFF292108)
    val OnPrimary = Color(0xFF1F1A00)
    val OnBackground = Color(0xFFFFFFFF)
    val OnSurface = Color(0xFFFFF3CD)
    val Accent = Color(0xFFF59E0B)
}

// 5. 浪漫甜蜜 (柔美粉)
object SweetPink {
    val Primary = Color(0xFFEC4899)
    val Secondary = Color(0xFFF472B6)
    val Background = Color(0xFF1F0A14)
    val Surface = Color(0xFF2D1520)
    val OnPrimary = Color(0xFFFFFFFF)
    val OnBackground = Color(0xFFFFFFFF)
    val OnSurface = Color(0xFFFCE7F3)
    val Accent = Color(0xFFDB2777)
}

// 6. 自然清新 (草地绿)
object NatureGreen {
    val Primary = Color(0xFF22C55E)
    val Secondary = Color(0xFF4ADE80)
    val Background = Color(0xFF0A1F0A)
    val Surface = Color(0xFF152D15)
    val OnPrimary = Color(0xFFFFFFFF)
    val OnBackground = Color(0xFFFFFFFF)
    val OnSurface = Color(0xFFDCFCE7)
    val Accent = Color(0xFF16A34A)
}

// 7. 高贵典雅 (皇室紫)
object RoyalPurple {
    val Primary = Color(0xFF8B5CF6)
    val Secondary = Color(0xFFA78BFA)
    val Background = Color(0xFF110F1A)
    val Surface = Color(0xFF1E1A2E)
    val OnPrimary = Color(0xFFFFFFFF)
    val OnBackground = Color(0xFFFFFFFF)
    val OnSurface = Color(0xFFEDE9FE)
    val Accent = Color(0xFF7C3AED)
}

// 8. 神秘深邃 (暗夜黑)
object MidnightBlack {
    val Primary = Color(0xFF6366F1)
    val Secondary = Color(0xFF818CF8)
    val Background = Color(0xFF0A0A0A)
    val Surface = Color(0xFF171717)
    val OnPrimary = Color(0xFFFFFFFF)
    val OnBackground = Color(0xFFE5E5E5)
    val OnSurface = Color(0xFFC4C4C4)
    val Accent = Color(0xFF4F46E5)
}

// 9. 温暖柔和 (暖橙)
object WarmOrange {
    val Primary = Color(0xFFF97316)
    val Secondary = Color(0xFFFB923C)
    val Background = Color(0xFF1A0F00)
    val Surface = Color(0xFF2D1A0A)
    val OnPrimary = Color(0xFFFFFFFF)
    val OnBackground = Color(0xFFFFFFFF)
    val OnSurface = Color(0xFFFFE4CC)
    val Accent = Color(0xFFEA580C)
}

// 10. 清新淡雅 (薄荷绿)
object MintGreen {
    val Primary = Color(0xFF14B8A6)
    val Secondary = Color(0xFF2DD4BF)
    val Background = Color(0xFF0A1A18)
    val Surface = Color(0xFF152C2A)
    val OnPrimary = Color(0xFFFFFFFF)
    val OnBackground = Color(0xFFFFFFFF)
    val OnSurface = Color(0xFFCCFBF1)
    val Accent = Color(0xFF0D9488)
}

enum class MoodTheme(val displayName: String) {
    PASSION_RED("热情洋溢"),
    MELANCHOLY_GRAY("沉默哀伤"),
    CALM_BLUE("平静宁和"),
    SUNNY_YELLOW("活力阳光"),
    SWEET_PINK("浪漫甜蜜"),
    NATURE_GREEN("自然清新"),
    ROYAL_PURPLE("高贵典雅"),
    MIDNIGHT_BLACK("神秘深邃"),
    WARM_ORANGE("温暖柔和"),
    MINT_GREEN("清新淡雅")
}

fun getMoodColors(theme: MoodTheme): MoodColors {
    return when (theme) {
        MoodTheme.PASSION_RED -> MoodColors(
            primary = PassionRed.Primary,
            secondary = PassionRed.Secondary,
            background = PassionRed.Background,
            surface = PassionRed.Surface,
            onPrimary = PassionRed.OnPrimary,
            onBackground = PassionRed.OnBackground,
            onSurface = PassionRed.OnSurface,
            accent = PassionRed.Accent
        )
        MoodTheme.MELANCHOLY_GRAY -> MoodColors(
            primary = MelancholyGray.Primary,
            secondary = MelancholyGray.Secondary,
            background = MelancholyGray.Background,
            surface = MelancholyGray.Surface,
            onPrimary = MelancholyGray.OnPrimary,
            onBackground = MelancholyGray.OnBackground,
            onSurface = MelancholyGray.OnSurface,
            accent = MelancholyGray.Accent
        )
        MoodTheme.CALM_BLUE -> MoodColors(
            primary = CalmBlue.Primary,
            secondary = CalmBlue.Secondary,
            background = CalmBlue.Background,
            surface = CalmBlue.Surface,
            onPrimary = CalmBlue.OnPrimary,
            onBackground = CalmBlue.OnBackground,
            onSurface = CalmBlue.OnSurface,
            accent = CalmBlue.Accent
        )
        MoodTheme.SUNNY_YELLOW -> MoodColors(
            primary = SunnyYellow.Primary,
            secondary = SunnyYellow.Secondary,
            background = SunnyYellow.Background,
            surface = SunnyYellow.Surface,
            onPrimary = SunnyYellow.OnPrimary,
            onBackground = SunnyYellow.OnBackground,
            onSurface = SunnyYellow.OnSurface,
            accent = SunnyYellow.Accent
        )
        MoodTheme.SWEET_PINK -> MoodColors(
            primary = SweetPink.Primary,
            secondary = SweetPink.Secondary,
            background = SweetPink.Background,
            surface = SweetPink.Surface,
            onPrimary = SweetPink.OnPrimary,
            onBackground = SweetPink.OnBackground,
            onSurface = SweetPink.OnSurface,
            accent = SweetPink.Accent
        )
        MoodTheme.NATURE_GREEN -> MoodColors(
            primary = NatureGreen.Primary,
            secondary = NatureGreen.Secondary,
            background = NatureGreen.Background,
            surface = NatureGreen.Surface,
            onPrimary = NatureGreen.OnPrimary,
            onBackground = NatureGreen.OnBackground,
            onSurface = NatureGreen.OnSurface,
            accent = NatureGreen.Accent
        )
        MoodTheme.ROYAL_PURPLE -> MoodColors(
            primary = RoyalPurple.Primary,
            secondary = RoyalPurple.Secondary,
            background = RoyalPurple.Background,
            surface = RoyalPurple.Surface,
            onPrimary = RoyalPurple.OnPrimary,
            onBackground = RoyalPurple.OnBackground,
            onSurface = RoyalPurple.OnSurface,
            accent = RoyalPurple.Accent
        )
        MoodTheme.MIDNIGHT_BLACK -> MoodColors(
            primary = MidnightBlack.Primary,
            secondary = MidnightBlack.Secondary,
            background = MidnightBlack.Background,
            surface = MidnightBlack.Surface,
            onPrimary = MidnightBlack.OnPrimary,
            onBackground = MidnightBlack.OnBackground,
            onSurface = MidnightBlack.OnSurface,
            accent = MidnightBlack.Accent
        )
        MoodTheme.WARM_ORANGE -> MoodColors(
            primary = WarmOrange.Primary,
            secondary = WarmOrange.Secondary,
            background = WarmOrange.Background,
            surface = WarmOrange.Surface,
            onPrimary = WarmOrange.OnPrimary,
            onBackground = WarmOrange.OnBackground,
            onSurface = WarmOrange.OnSurface,
            accent = WarmOrange.Accent
        )
        MoodTheme.MINT_GREEN -> MoodColors(
            primary = MintGreen.Primary,
            secondary = MintGreen.Secondary,
            background = MintGreen.Background,
            surface = MintGreen.Surface,
            onPrimary = MintGreen.OnPrimary,
            onBackground = MintGreen.OnBackground,
            onSurface = MintGreen.OnSurface,
            accent = MintGreen.Accent
        )
    }
}

data class MoodColors(
    val primary: Color,
    val secondary: Color,
    val background: Color,
    val surface: Color,
    val onPrimary: Color,
    val onBackground: Color,
    val onSurface: Color,
    val accent: Color
)
