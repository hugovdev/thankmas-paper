package me.hugo.thankmas.region

import me.hugo.thankmas.location.deserializeLocation
import me.hugo.thankmas.region.triggering.ActionableRegion
import me.hugo.thankmas.region.triggering.RegionAction
import me.hugo.thankmas.registry.MapBasedRegistry
import org.bukkit.configuration.file.FileConfiguration
import org.koin.core.annotation.Single

/**
 * Registry of all configured regions, also keeps track
 * of what players enter, leave and idle in them.
 */
@Single
public class RegionRegistry(
    config: FileConfiguration,
    normalRegionsPath: String,
    actionRegionsPath: String
) : MapBasedRegistry<String, Region>() {

    init {
        config.getConfigurationSection(normalRegionsPath)?.getKeys(false)?.forEach { regionId ->
            register(regionId, Region(config, "$normalRegionsPath.$regionId"))
        }

        config.getConfigurationSection(actionRegionsPath)?.getKeys(false)?.forEach { regionId ->
            val fullPath = "$normalRegionsPath.$regionId"
            val normalRegion = Region(config, fullPath)

            val enterAction = config.getString("$fullPath.enter-action")?.let { RegionAction.valueOf(it) }
            val idleAction = config.getString("$fullPath.idle-action")?.let { RegionAction.valueOf(it) }
            val leaveAction = config.getString("$fullPath.leave-action")?.let { RegionAction.valueOf(it) }

            register(regionId, ActionableRegion(normalRegion, enterAction, idleAction, leaveAction))
        }
    }
}