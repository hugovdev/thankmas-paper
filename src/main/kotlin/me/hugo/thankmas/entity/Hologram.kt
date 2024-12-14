package me.hugo.thankmas.entity

import dev.kezz.miniphrase.MiniPhraseContext
import me.hugo.thankmas.ThankmasPlugin
import me.hugo.thankmas.markers.Marker
import me.hugo.thankmas.player.translate
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.entity.TextDisplay.TextAlignment
import org.bukkit.event.entity.CreatureSpawnEvent
import java.util.*

/** TextDisplay that shows different text per player. */
public class Hologram(
    private val location: Location,
    private val propertiesSupplier: (viewer: Player, preferredLocale: Locale?) -> HologramProperties,
    private val textSupplier: (viewer: Player, preferredLocale: Locale?) -> Component
) {

    private val playerDataManager = ThankmasPlugin.instance().playerDataManager

    public companion object {
        context(MiniPhraseContext)
        public fun fromMarker(
            marker: Marker
        ): Hologram {
            val properties = HologramProperties(
                Display.Billboard.valueOf(marker.getString("billboard")?.uppercase() ?: "FIXED"),
                marker.getIntList("brightness")?.let {
                    Display.Brightness(it[0], it[1])
                } ?: Display.Brightness(15, 15),
                TextAlignment.valueOf(marker.getString("text_alignment") ?: "LEFT"),
                marker.getInt("line_width") ?: 200,
                marker.getBoolean("see_through") ?: false,
                marker.getBoolean("text_shadow") ?: false,
            )

            return Hologram(
                marker.location.toLocation(marker.world),
                propertiesSupplier = { _, _ -> properties },
                textSupplier = { player, locale ->
                    player.translate(marker.getString("text") ?: "hologram.error", locale)
                }
            )
        }
    }

    /** Spawns this hologram to [player]. */
    public fun spawnOrUpdate(player: Player, locale: Locale? = null) {
        val playerData = playerDataManager.getPlayerData(player.uniqueId)

        // If the hologram has already spawned for [player], we just change the text and properties.
        val originalDisplay = playerData.getDisplayForHologramOrNull(this)

        if (originalDisplay != null) {
            originalDisplay.text(textSupplier(player, locale))
            propertiesSupplier(player, locale).apply(originalDisplay)
            return
        }

        val textDisplay =
            location.world.spawnEntity(location, EntityType.TEXT_DISPLAY, CreatureSpawnEvent.SpawnReason.CUSTOM) {
                it as TextDisplay
                it.isVisibleByDefault = false
                it.isPersistent = true

                it.text(textSupplier(player, locale))
                propertiesSupplier(player, locale).apply(it)
            } as TextDisplay

        player.showEntity(ThankmasPlugin.instance(), textDisplay)
        playerData.addHologram(this, textDisplay)
    }

    /** Removes this hologram from the [player]'s client. */
    public fun remove(player: Player) {
        val playerData = playerDataManager.getPlayerDataOrNull(player.uniqueId)
        val textDisplay = playerData?.getDisplayForHologramOrNull(this) ?: return

        textDisplay.remove()
        playerData.removeHologram(this)
    }

    /** All important hologram properties to keep track of. */
    public data class HologramProperties(
        private val billboardRotation: Display.Billboard,
        private val brightness: Display.Brightness? = null,
        private val textAlignment: TextAlignment? = null,
        private val lineWidth: Int? = null,
        private val textSeeThrough: Boolean = false,
        private val textShadow: Boolean = false,
    ) {
        public fun apply(display: TextDisplay) {
            display.billboard = billboardRotation
            display.brightness = brightness
            textAlignment?.let { display.alignment = it }
            lineWidth?.let { display.lineWidth = it }
            display.isSeeThrough = textSeeThrough
            display.isShadowed = textShadow
        }
    }
}