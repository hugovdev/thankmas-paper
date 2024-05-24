package me.hugo.thankmas.markers

import me.hugo.thankmas.location.MapPoint
import me.hugo.thankmas.region.Region
import org.bukkit.Bukkit
import org.bukkit.World

/** Base marker entity. */
public abstract class Marker(public val location: MapPoint, public val worldName: String) {

    public val world: World
        get() = requireNotNull(Bukkit.getWorld(worldName))
        { "Tried to use marker's world object but it's not loaded!" }

    /** Returns the name or id of this marker. */
    public abstract fun getMarkerId(): String

    /** Gets a string from a key. */
    public abstract fun getString(key: String): String?

    /** Gets a boolean from a key. */
    public abstract fun getBoolean(key: String): Boolean?

    /** Gets an integer from a key. */
    public abstract fun getInt(key: String): Int?

    /** Gets a double from a key. */
    public abstract fun getDouble(key: String): Double?

    /** Gets a double from a key. */
    public abstract fun getFloat(key: String): Float?

    /** Returns a region out of the Axiom box data fields. */
    public abstract fun toRegion(world: World): Region

    /** Gets a map point from a list of doubles in a data field. */
    public abstract fun getMapPoint(key: String): MapPoint
}