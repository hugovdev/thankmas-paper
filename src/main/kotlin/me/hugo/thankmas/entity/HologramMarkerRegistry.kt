package me.hugo.thankmas.entity

import me.hugo.thankmas.lang.TranslatedComponent
import me.hugo.thankmas.markers.Marker
import me.hugo.thankmas.registry.MapBasedRegistry
import me.hugo.thankmas.world.registry.AnvilWorldRegistry
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.koin.core.component.inject

/** Registers all holograms in markers and spawns them to players. */
public class HologramMarkerRegistry(world: String) : MapBasedRegistry<Marker, Hologram>(), Listener,
    TranslatedComponent {

    private val anvilWorldRegistry: AnvilWorldRegistry by inject()

    init {
        anvilWorldRegistry.getMarkerForType(world, "hologram").forEach {
            register(it, Hologram.fromMarker(it))
        }
    }

    @EventHandler
    public fun onPlayerJoin(event: PlayerJoinEvent) {
        getValues().forEach { it.spawnOrUpdate(event.player) }
    }
}