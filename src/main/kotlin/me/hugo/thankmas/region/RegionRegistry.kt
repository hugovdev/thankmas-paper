package me.hugo.thankmas.region

import me.hugo.thankmas.ThankmasPlugin
import me.hugo.thankmas.player.PaperPlayerData
import me.hugo.thankmas.player.PlayerDataManager
import me.hugo.thankmas.registry.MapBasedRegistry
import org.bukkit.Bukkit
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent

/**
 * Registry of all configured regions, also keeps track
 * of what players enter, leave and idle in them.
 */
@Single
public class RegionRegistry(playerDataRegistry: PlayerDataManager<PaperPlayerData>) :
    MapBasedRegistry<String, Region>(), KoinComponent {

    init {
        Bukkit.getScheduler().runTaskTimer(ThankmasPlugin.instance(), Runnable {
            Bukkit.getOnlinePlayers().forEach { player ->
                val playerData = playerDataRegistry.getPlayerDataOrNull(player.uniqueId) ?: return@forEach

                getValues().forEach { region ->
                    if (region.contains(player.location)) playerData.updateOnRegion(region)
                    else playerData.leaveRegion(region)
                }
            }
        }, 10L, 2L)
    }
}