package me.hugo.thankmas.region

import me.hugo.thankmas.ThankmasPlugin
import me.hugo.thankmas.config.ConfigurationProvider
import me.hugo.thankmas.player.PaperPlayerData
import me.hugo.thankmas.player.PlayerDataManager
import me.hugo.thankmas.region.triggering.ActionableRegion
import me.hugo.thankmas.region.triggering.RegionAction
import me.hugo.thankmas.registry.MapBasedRegistry
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Registry of all configured regions, also keeps track
 * of what players enter, leave and idle in them.
 */
@Single
public class RegionRegistry(playerDataRegistry: PlayerDataManager<PaperPlayerData>) :
    MapBasedRegistry<String, Region>(), KoinComponent {

    private val configProvider: ConfigurationProvider by inject()

    init {
        val config = configProvider.getOrLoad("regions", "../global/")

        config.getConfigurationSection("regions")?.getKeys(false)?.forEach { regionId ->
            register(regionId, Region(config, "regions.$regionId"))
        }

        config.getConfigurationSection("action-regions")?.getKeys(false)?.forEach { regionId ->
            val fullPath = "action-regions.$regionId"
            val normalRegion = Region(config, fullPath)

            val enterAction = config.getString("$fullPath.enter-action")?.let { RegionAction.valueOf(it) }
            val idleAction = config.getString("$fullPath.idle-action")?.let { RegionAction.valueOf(it) }
            val leaveAction = config.getString("$fullPath.leave-action")?.let { RegionAction.valueOf(it) }

            register(regionId, ActionableRegion(normalRegion, enterAction, idleAction, leaveAction))
        }

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