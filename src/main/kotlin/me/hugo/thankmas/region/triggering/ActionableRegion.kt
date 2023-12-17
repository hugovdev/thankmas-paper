package me.hugo.thankmas.region.triggering

import me.hugo.thankmas.location.serializeString
import me.hugo.thankmas.region.Region
import org.bukkit.Location
import org.bukkit.configuration.file.FileConfiguration

/**
 * Region that uses RegionActions as runnables for
 * entering, leaving and idling.
 */
public class ActionableRegion(
    regionId: String, corner1: Location, corner2: Location,
    public val enterAction: RegionAction? = null,
    public val idleAction: RegionAction? = null,
    public val leaveAction: RegionAction? = null,
) : TriggeringRegion(regionId, corner1, corner2, enterAction?.action, idleAction?.action, leaveAction?.action) {

    /** Constructor that takes a normal region as a parameter. */
    public constructor(
        region: Region,
        enterAction: RegionAction? = null,
        idleAction: RegionAction? = null,
        leaveAction: RegionAction? = null,
    ) : this(region.id, region.corner1, region.corner2, enterAction, idleAction, leaveAction)

    /**
     * Saves this region into [config] at [path].
     */
    public override fun saveInConfig(config: FileConfiguration, path: String?) {
        val finalPath = path ?: "actionable-regions"

        config.set("$finalPath.region-id", id)
        config.set("$finalPath.corner1", corner1.serializeString())
        config.set("$finalPath.corner2", corner1.serializeString())

        config.set("$finalPath.enter-action", enterAction?.name)
        config.set("$finalPath.idle-action", idleAction?.name)
        config.set("$finalPath.leave-action", leaveAction?.name)

        val configName = configProvider.getNameFromConfig(config)
        requireNotNull(configName) { "Could not save region to ${config.name} because the original config name could not be found." }

        val file = configProvider.getFile(configName)
        requireNotNull(file) { "Could not save region to $configName because the original file could not be found in cache." }

        config.save(file)
    }

}