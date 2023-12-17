package me.hugo.thankmas.region.triggering

import me.hugo.thankmas.region.Region
import org.bukkit.Location
import org.bukkit.entity.Player

/**
 * Region that has runnables for entering, leaving and
 * idling on this region.
 */
public open class TriggeringRegion(
    regionId: String, corner1: Location, corner2: Location,
    /** Runs for a player when they enter this region. */
    public val onEnter: ((player: Player) -> Unit)?,
    /** Runs for every player inside this region. */
    public val onIdle: ((player: Player) -> Unit)?,
    /** Runs for a player who leaves this region. */
    public val onLeave: ((player: Player) -> Unit)?
) : Region(regionId, corner1, corner2) {

    /** Constructor that takes a normal region as a parameter. */
    public constructor(
        region: Region,
        onEnter: ((player: Player) -> Unit)?,
        onIdle: ((player: Player) -> Unit)?,
        onLeave: ((player: Player) -> Unit)?
    ) : this(region.id, region.corner1, region.corner2, onEnter, onIdle, onLeave)
}
