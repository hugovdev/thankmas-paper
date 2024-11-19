package me.hugo.thankmas.listener

import me.hugo.thankmas.markers.registry.MarkerRegistry
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.spigotmc.event.player.PlayerSpawnLocationEvent

/** Spawns players in the marker named [markerName] in the world [world]. */
@Single
public class PlayerSpawnpointOnJoin(private val worldName: String, private val markerName: String) : Listener,
    KoinComponent {

    private val markerRegistry: MarkerRegistry by inject()

    public val spawnpoint: Location by lazy {
        requireNotNull(Bukkit.getWorld(worldName)?.let {
            markerRegistry.getMarkerForType(markerName, worldName).firstOrNull()
                ?.location?.toLocation(it)
        }) { "Tried to spawn player in $markerName marker in $worldName, but couldn't!" }
    }

    @EventHandler
    private fun onSpawnDeciding(event: PlayerSpawnLocationEvent) {
        // Try to teleport the player to the hub_spawnpoint marker.
        event.spawnLocation = spawnpoint
    }

}