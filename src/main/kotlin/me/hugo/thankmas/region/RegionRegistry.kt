package me.hugo.thankmas.region

import me.hugo.thankmas.location.deserializeLocation
import me.hugo.thankmas.region.triggering.ActionableRegion
import me.hugo.thankmas.region.triggering.RegionAction
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
) {

    /** Loaded regions and their ids. */
    private val regions: MutableMap<String, Region> = mutableMapOf()

    init {
        config.getConfigurationSection(normalRegionsPath)?.getKeys(false)?.forEach { regionId ->
            register(getRegion(config, "$normalRegionsPath.$regionId"))
        }

        config.getConfigurationSection(actionRegionsPath)?.getKeys(false)?.forEach { regionId ->
            val fullPath = "$normalRegionsPath.$regionId"
            val normalRegion = getRegion(config, fullPath)

            val enterAction = config.getString("$fullPath.enter-action")?.let { RegionAction.valueOf(it) }
            val idleAction = config.getString("$fullPath.idle-action")?.let { RegionAction.valueOf(it) }
            val leaveAction = config.getString("$fullPath.leave-action")?.let { RegionAction.valueOf(it) }

            register(ActionableRegion(normalRegion, enterAction, idleAction, leaveAction))
        }
    }

    /** Registers [region]. */
    public fun register(region: Region) {
        regions[region.id] = region
    }

    /** @returns a region form a config file and path. */
    private fun getRegion(config: FileConfiguration, fullPath: String): Region {
        val regionId = config.getString("$fullPath.region-id")
        requireNotNull(regionId) { "Region id is missing from a config entry!" }

        val serializedCorner1 = config.getString("$fullPath.corner1")
        val serializedCorner2 = config.getString("$fullPath.corner2")

        requireNotNull(serializedCorner1) { "$fullPath is missing the first corner!" }
        requireNotNull(serializedCorner2) { "$fullPath is missing the second corner!" }

        return Region(regionId, serializedCorner1.deserializeLocation(), serializedCorner2.deserializeLocation())
    }

}