package com.xrotune.music.core.common

import java.util.concurrent.TimeUnit

object Formatters {

    fun duration(seconds: Long): String {
        if (seconds <= 0) return "0:00"
        val h = TimeUnit.SECONDS.toHours(seconds)
        val m = TimeUnit.SECONDS.toMinutes(seconds) % 60
        val s = seconds % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
    }

    fun count(value: Long): String = when {
        value >= 1_000_000_000 -> "%.1fB".format(value / 1_000_000_000.0)
        value >= 1_000_000 -> "%.1fM".format(value / 1_000_000.0)
        value >= 1_000 -> "%.1fK".format(value / 1_000.0)
        else -> value.toString()
    }

    fun bytes(value: Long): String = when {
        value >= 1L shl 30 -> "%.2f GB".format(value.toDouble() / (1L shl 30))
        value >= 1L shl 20 -> "%.1f MB".format(value.toDouble() / (1L shl 20))
        value >= 1L shl 10 -> "%.0f KB".format(value.toDouble() / (1L shl 10))
        else -> "$value B"
    }

    fun timeAgo(epochSeconds: Long): String {
        if (epochSeconds <= 0) return ""
        val diff = System.currentTimeMillis() / 1000 - epochSeconds
        return when {
            diff < 60 -> "just now"
            diff < 3600 -> "${diff / 60}m ago"
            diff < 86400 -> "${diff / 3600}h ago"
            diff < 2_592_000 -> "${diff / 86400}d ago"
            else -> "${diff / 2_592_000}mo ago"
        }
    }
}
