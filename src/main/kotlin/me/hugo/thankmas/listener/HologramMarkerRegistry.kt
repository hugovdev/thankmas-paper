package me.hugo.thankmas.listener

import me.hugo.thankmas.entity.Hologram
import me.hugo.thankmas.lang.TranslatedComponent
import me.hugo.thankmas.markers.Marker
import me.hugo.thankmas.markers.registry.MarkerRegistry
import me.hugo.thankmas.player.PaperPlayerData
import me.hugo.thankmas.player.PlayerDataManager
import me.hugo.thankmas.registry.MapBasedRegistry
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.koin.core.component.inject

/** Registers all holograms in markers and spawns them to players. */
public class HologramMarkerRegistry<P : PaperPlayerData>(
    private val world: String,
    private val playerDataManager: PlayerDataManager<P>
) : MapBasedRegistry<Marker, Hologram<*>>(),
    Listener,
    TranslatedComponent {

    private val markerRegistry: MarkerRegistry by inject()

    init {
        markerRegistry.getMarkerForType("hologram", world).forEach {
            register(it, Hologram.fromMarker(it, playerDataManager))
        }
    }

    @EventHandler
    public fun onPlayerJoin(event: PlayerJoinEvent) {
        getValues().forEach { it.spawnOrUpdate(event.player) }
    }

    @EventHandler(priority = EventPriority.LOW)
    private fun onPlayerLeave(event: PlayerQuitEvent) {
        playerDataManager.getPlayerData(event.player.uniqueId).removeAllHolograms()
    }
}