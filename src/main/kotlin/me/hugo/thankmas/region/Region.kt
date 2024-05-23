package me.hugo.thankmas.region

import me.hugo.thankmas.config.ConfigurationProvider
import me.hugo.thankmas.config.string
import me.hugo.thankmas.location.deserializeLocation
import me.hugo.thankmas.location.serializeString
import me.hugo.thankmas.region.triggering.TriggeringRegion
import org.bukkit.Location
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.math.max
import kotlin.math.min

/**
 * A region composed of two corners.
 */
public open class Region(public val id: String, public val corner1: Location, public val corner2: Location) :
    KoinComponent {

    /**
     * Loads a region from [config] in [path].
     */
    public constructor(config: FileConfiguration, path: String) : this(
        config.string("$path.region-id"),
        config.string("$path.corner1").deserializeLocation(),
        config.string("$path.corner2").deserializeLocation()
    )

    protected val configProvider: ConfigurationProvider by inject()

    /** Location composed of the smallest coordinates. */
    public val minCorner: Location =
        Location(corner1.world, min(corner1.x, corner2.x), min(corner1.y, corner2.y), min(corner1.z, corner2.z))

    /** Location composed of the highest coordinates. */
    public val maxCorner: Location =
        Location(corner1.world, max(corner1.x, corner2.x), max(corner1.y, corner2.y), max(corner1.z, corner2.z))

    /** @returns whether a location is inside this region. */
    public operator fun contains(location: Location): Boolean {
        return location.x() >= minCorner.x() && location.x() <= maxCorner.x() &&
                location.z() >= minCorner.z() && location.z() <= maxCorner.z() &&
                location.y() >= minCorner.y() && location.y() <= maxCorner.y()
    }

    /**
     * Transforms this region to a triggering region with
     * custom runnables.
     */
    public fun toTriggering(
        onEnter: ((player: Player) -> Unit)? = null,
        onIdle: ((player: Player) -> Unit)? = null,
        onLeave: ((player: Player) -> Unit)? = null
    ): TriggeringRegion {
        val registry: RegionRegistry by inject()
        val triggeringRegion = TriggeringRegion(this, onEnter, onIdle, onLeave)

        registry.register(this.id, triggeringRegion)

        return triggeringRegion
    }

    /**
     * Saves this region into [config] at [path].
     */
    public open fun saveInConfig(config: FileConfiguration, path: String?) {
        val finalPath = path ?: "regions"

        config.set("$finalPath.region-id", id)
        config.set("$finalPath.corner1", corner1.serializeString())
        config.set("$finalPath.corner2", corner1.serializeString())

        val configName = configProvider.getNameFromConfig(config)
        requireNotNull(configName) { "Could not save region to ${config.name} because the original config name could not be found." }

        val file = configProvider.getFile(configName)
        requireNotNull(file) { "Could not save region to $configName because the original file could not be found in cache." }

        config.save(file)
    }
}