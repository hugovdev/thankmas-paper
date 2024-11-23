package me.hugo.thankmas.region

import me.hugo.thankmas.config.ConfigurationProvider
import me.hugo.thankmas.markers.Marker
import org.bukkit.Location
import org.bukkit.World
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** A region composed of two corners attached to a specific world. */
public open class WorldRegion(
    marker: Marker,
    id: String,
    public val world: World
) : WeakRegion(marker, id), KoinComponent {

    public constructor(marker: Marker, world: World) : this(marker, marker.getMarkerId(), world)

    private val minCornerLocation: Location = minCorner.toLocation(world)
    private val maxCornerLocation: Location = maxCorner.toLocation(world)

    protected val configProvider: ConfigurationProvider by inject()

    /** @returns whether a location is inside this region. */
    public override operator fun contains(location: Location): Boolean {
        return location.world == world && location.x() >= minCornerLocation.x() && location.x() <= maxCornerLocation.x() &&
                location.z() >= minCornerLocation.z() && location.z() <= maxCornerLocation.z() &&
                location.y() >= minCornerLocation.y() && location.y() <= maxCornerLocation.y()
    }

    /** Registers this region so enter, idle and leave functions are called accordingly. */
    public fun register() {
        val regionRegistry: RegionRegistry by inject()
        regionRegistry.register(id, this)
    }
}