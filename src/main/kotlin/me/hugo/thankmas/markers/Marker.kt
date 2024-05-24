package me.hugo.thankmas.markers

import me.hugo.thankmas.location.MapPoint
import me.hugo.thankmas.region.Region
import org.bukkit.Bukkit
import org.bukkit.World
import org.jglrxavpok.hephaistos.nbt.NBTCompound
import org.jglrxavpok.hephaistos.nbt.NBTDouble

/** Marker entity with nbt data placed in the map. */
public data class Marker(val location: MapPoint, val worldName: String, val data: NBTCompound) {

    public val world: World
        get() = requireNotNull(Bukkit.getWorld(worldName))
        { "Tried to use marker's world object but it's not loaded!" }

    /**
     * Returns the name or id of this marker.
     *
     * We can safely remove null safety because we check
     * the name field before registering any Markers.
     */
    public fun getMarkerId(): String {
        return data.getString("name")!!
    }

    /** Returns a region out of the Axiom box data fields. */
    public fun toRegion(world: World): Region {
        return Region(getMarkerId(), getMapPoint("min").toLocation(world), getMapPoint("max").toLocation(world))
    }

    /** Gets a map point from a list of doubles in a data field. */
    public fun getMapPoint(key: String): MapPoint {
        val coordinateList = requireNotNull(data.getList<NBTDouble>(key))
        { "Tried to get map point with key $key but no data was found." }

        return if (coordinateList.size == 3) {
            MapPoint(coordinateList[0].value, coordinateList[1].value, coordinateList[2].value, 0.0f, 0.0f)
        } else MapPoint(
            coordinateList[0].value, coordinateList[1].value, coordinateList[2].value,
            coordinateList[3].value.toFloat(),
            coordinateList[4].value.toFloat()
        )
    }

}