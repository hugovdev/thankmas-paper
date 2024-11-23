package me.hugo.thankmas.region

import me.hugo.thankmas.lang.TranslatedComponent
import me.hugo.thankmas.location.MapPoint
import me.hugo.thankmas.markers.Marker
import org.bukkit.Location
import org.bukkit.entity.Player

/**
 * Region with no references to a world.
 * It only uses MapPoints.
 */
public open class WeakRegion(
    public val id: String,
    protected val minCorner: MapPoint,
    protected val maxCorner: MapPoint,
) : TranslatedComponent {

    public constructor(marker: Marker, id: String) : this(
        id,
        requireNotNull(marker.getMapPoint("min")),
        requireNotNull(marker.getMapPoint("max"))
    )

    public constructor(marker: Marker) : this(
        requireNotNull(marker.getString("id"))
        { "Tried to create a WeakRegion region without an id!" },
        requireNotNull(marker.getMapPoint("min")),
        requireNotNull(marker.getMapPoint("max"))
    )

    public open fun onEnter(player: Player) {}
    public open fun onIdle(player: Player) {}
    public open fun onLeave(player: Player) {}

    /** @returns whether a location is inside this region. */
    public open operator fun contains(location: Location): Boolean {
        return location.x() >= minCorner.x && location.x() <= maxCorner.x &&
                location.z() >= minCorner.z && location.z() <= maxCorner.z &&
                location.y() >= minCorner.y && location.y() <= maxCorner.y
    }
}