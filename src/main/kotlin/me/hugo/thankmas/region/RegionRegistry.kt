package me.hugo.thankmas.region

import me.hugo.thankmas.ThankmasPlugin
import me.hugo.thankmas.player.PaperPlayerData
import me.hugo.thankmas.player.PlayerDataManager
import me.hugo.thankmas.registry.MapBasedRegistry
import org.bukkit.Bukkit
import org.koin.core.component.KoinComponent

/**
 * Registry of all configured regions, also keeps track
 * of what players enter, leave and idle in them.
 */
public class RegionRegistry<P : PaperPlayerData<P>>(playerDataRegistry: PlayerDataManager<P>) :
    MapBasedRegistry<String, Region>(), KoinComponent {

    init {
        Bukkit.getScheduler().runTaskTimer(ThankmasPlugin.instance(), Runnable {
            playerDataRegistry.getAllPlayerData().forEach { player ->
                val onlinePlayer = player.onlinePlayerOrNull ?: return@forEach
                getValues().forEach { region ->
                    if (region.contains(onlinePlayer.location)) player.updateOnRegion(region)
                    else player.leaveRegion(region)
                }
            }
        }, 10L, 2L)
    }
}