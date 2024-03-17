package me.hugo.thankmas.math

import me.hugo.thankmas.ThankmasPlugin
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

public fun Long.formatToTime(viewer: Player): Component {
    val seconds = (this / 1000).toInt() % 60
    val minutes = ((this / (1000 * 60)) % 60).toInt()
    val hours = ((this / (1000 * 60 * 60)) % 24).toInt()

    return ThankmasPlugin.instance().globalTranslations.translate(
        if (hours > 0) "general.time.hours"
        else if (minutes > 0) "general.time.minutes"
        else "general.time.seconds",
        viewer.locale()
    ) {
        parsed("hours", hours)
        parsed("minutes", minutes)
        parsed("seconds", seconds)
    }
}