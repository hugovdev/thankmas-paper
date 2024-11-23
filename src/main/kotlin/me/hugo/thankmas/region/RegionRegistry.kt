package me.hugo.thankmas.region

import me.hugo.thankmas.ThankmasPlugin
import me.hugo.thankmas.registry.MapBasedRegistry
import org.bukkit.Bukkit
import org.koin.core.annotation.Single

/**
 * Registry of all configured regions, also keeps track
 * of what players enter, leave and idle in them.
 */
@Single
public class RegionRegistry : MapBasedRegistry<String, WorldRegion>() {

    private val instance = ThankmasPlugin.instance()
    private val playerDataManager = instance.playerDataManager

    init {
        Bukkit.getScheduler().runTaskTimer(instance, Runnable {
            getValues().forEach { region ->
                region.world.players.forEach players@{ player ->
                    val playerData = playerDataManager.getPlayerDataOrNull(player.uniqueId) ?: return@players

                    if (player.location in region) playerData.updateOnRegion(region)
                    else playerData.leaveRegion(region)
                }
            }
        }, 0L, 1L)
    }
}