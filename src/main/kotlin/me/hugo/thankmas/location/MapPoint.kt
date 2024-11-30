package me.hugo.thankmas.location

import org.bukkit.Location
import org.bukkit.World
import kotlin.math.abs
import kotlin.math.floor

/**
 * Object that contains the exact location of certain
 * place in a map but doesn't contain a specific world.
 */
public class MapPoint(
    public val x: Double,
    public val y: Double,
    public val z: Double,
    public val yaw: Float,
    public val pitch: Float
) {

    /**
     * Creates a [MapPoint] from a [location]
     *
     * If [centerToBlock] it also centers the [location]
     * given to the center of the block. Also moves the
     * yaw to the closest full rotation and locks the
     * pitch to 0.0 (looking forward).
     */
    public constructor(location: Location, centerToBlock: Boolean = true) : this(
        if (centerToBlock) floor(location.x) + 0.5 else location.x,
        location.y,
        if (centerToBlock) floor(location.z) + 0.5 else location.z,
        if (centerToBlock) {
            listOf(-180.0f, -90.0f, 0.0f, 90.0f, 180.0f).minBy { v -> abs(v - location.yaw) }
        } else location.yaw,
        if (centerToBlock) 0.0f else location.pitch
    )

    public companion object {

        /** @returns a deserialized [MapPoint] from a [serializedPoint]. */
        public fun deserialize(serializedPoint: String): MapPoint {
            val split = serializedPoint.split(" , ")

            requireNotNull(split.size == 5) { "MapPoint \"$serializedPoint\" doesn't follow the correct format." }

            return MapPoint(
                split[0].toDouble(), split[1].toDouble(), split[2].toDouble(),
                split[3].toFloat(), split[4].toFloat()
            )
        }

    }

    /**
     * Returns a bukkit location with this
     * MapPoint's coordinates in [world].
     */
    public fun toLocation(world: World?): Location {
        return Location(world, x, y, z, yaw, pitch)
    }

    /**
     * Serializes this MapPoint into a String with
     * format: "x , y , z , yaw , pitch".
     */
    public fun serialize(): String {
        return "$x , $y , $z , $yaw , $pitch"
    }
}