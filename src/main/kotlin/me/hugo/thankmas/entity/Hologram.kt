package me.hugo.thankmas.entity

import dev.kezz.miniphrase.MiniPhraseContext
import me.hugo.thankmas.ThankmasPlugin
import me.hugo.thankmas.markers.Marker
import me.hugo.thankmas.player.PaperPlayerData
import me.hugo.thankmas.player.PlayerDataManager
import me.hugo.thankmas.player.translate
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.entity.TextDisplay.TextAlignment
import org.bukkit.event.entity.CreatureSpawnEvent
import org.jglrxavpok.hephaistos.nbt.NBTInt
import java.util.*

/** TextDisplay that shows different text per player. */
public class Hologram<P : PaperPlayerData>(
    private val location: Location,
    private val propertiesSupplier: (viewer: Player, preferredLocale: Locale?) -> HologramProperties,
    private val textSupplier: (viewer: Player, preferredLocale: Locale?) -> Component,
    private val playerManager: PlayerDataManager<P>
) {

    public companion object {
        context(MiniPhraseContext)
        public fun <P : PaperPlayerData> fromMarker(
            marker: Marker,
            playerDataManager: PlayerDataManager<P>
        ): Hologram<P> {
            val properties = HologramProperties(
                Display.Billboard.valueOf(marker.data.getString("billboard")?.uppercase() ?: "FIXED"),
                marker.data.getList<NBTInt>("brightness")?.let {
                    Display.Brightness(it[0].value, it[1].value)
                } ?: Display.Brightness(15, 15),
                TextAlignment.valueOf(marker.data.getString("text_alignment") ?: "LEFT"),
                marker.data.getInt("line_width") ?: 200,
                marker.data.getBoolean("see_through") ?: false,
                marker.data.getBoolean("text_shadow") ?: false,
            )

            return Hologram(
                marker.location.toLocation(marker.world),
                propertiesSupplier = { _, _ -> properties },
                textSupplier = { player, locale ->
                    player.translate(marker.data.getString("text") ?: "hologram.error", locale)
                },
                playerDataManager
            )
        }
    }

    /** Spawns this hologram to [player]. */
    public fun spawnOrUpdate(player: Player, locale: Locale? = null) {
        val playerData = playerManager.getPlayerData(player.uniqueId)

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

                it.text(textSupplier(player, locale))
                propertiesSupplier(player, locale).apply(it)
            } as TextDisplay

        player.showEntity(ThankmasPlugin.instance(), textDisplay)
        playerData.addHologram(this, textDisplay)
    }

    /** Removes this hologram from the [player]'s client. */
    public fun remove(player: Player) {
        val playerData = playerManager.getPlayerData(player.uniqueId)
        val textDisplay = playerData.getDisplayForHologramOrNull(this)
        requireNotNull(textDisplay) { "Tried to despawn a hologram from ${player.name}, who wasn't a viewer! " }

        textDisplay.remove()
    }

    /** All important hologram properties to keep track of. */
    public data class HologramProperties(
        private val billboardRotation: Display.Billboard,
        private val brightness: Display.Brightness,
        private val textAlignment: TextAlignment,
        private val lineWidth: Int,
        private val textSeeThrough: Boolean = false,
        private val textShadow: Boolean = false,
    ) {
        public fun apply(display: TextDisplay) {
            display.billboard = billboardRotation
            display.brightness = brightness
            display.alignment = textAlignment
            display.lineWidth = lineWidth
            display.isSeeThrough = textSeeThrough
            display.isShadowed = textShadow
        }
    }
}