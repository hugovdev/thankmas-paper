package me.hugo.thankmas.listener

import me.hugo.thankmas.ThankmasPlugin
import me.hugo.thankmas.world.registry.AnvilWorldRegistry
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.spigotmc.event.player.PlayerSpawnLocationEvent
import java.util.UUID

/** Spawns players in the marker named [markerName] in the world [world]. */
@Single
public class PlayerSpawnpointOnJoin(
    private val worldName: String,
    private val markerName: String,
    private val yLevelTeleport: Int? = null
) : Listener,
    KoinComponent {

    private val anvilWorldRegistry: AnvilWorldRegistry by inject()

    public val spawnpoint: Location by lazy {
        requireNotNull(Bukkit.getWorld(worldName)?.let {
            anvilWorldRegistry.getMarkerForType(worldName, markerName).firstOrNull()
                ?.location?.toLocation(it)
        }) { "Tried to spawn player in $markerName marker in $worldName, but couldn't!" }
    }

    private val teleportingPlayers: MutableSet<UUID> = mutableSetOf()

    init {
        if (yLevelTeleport != null) {
            Bukkit.getScheduler().runTaskTimer(ThankmasPlugin.instance(), Runnable {
                Bukkit.getOnlinePlayers()
                    .filter { it.location.y <= yLevelTeleport && !teleportingPlayers.contains(it.uniqueId) }.forEach {
                        teleportingPlayers.add(it.uniqueId)

                        it.teleportAsync(spawnpoint).thenRun {
                            teleportingPlayers -= it.uniqueId
                        }
                    }
            }, 0L, 20L)
        }
    }

    @EventHandler
    private fun onLeave(event: PlayerQuitEvent) {
        teleportingPlayers -= event.player.uniqueId
    }

    @EventHandler
    private fun onSpawnDeciding(event: PlayerSpawnLocationEvent) {
        // Try to teleport the player to the hub_spawnpoint marker.
        event.spawnLocation = spawnpoint
    }

}