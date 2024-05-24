package me.hugo.thankmas.markers

import me.hugo.thankmas.location.MapPoint
import me.hugo.thankmas.region.Region
import org.bukkit.World
import org.jglrxavpok.hephaistos.nbt.NBTCompound
import org.jglrxavpok.hephaistos.nbt.NBTDouble

public class VanillaMarker(location: MapPoint, worldName: String, public val data: NBTCompound) :
    Marker(location, worldName) {

    public override fun getMarkerId(): String {
        return data.getString("name")!!
    }

    override fun getString(key: String): String? = data.getString(key)

    override fun getBoolean(key: String): Boolean? = data.getBoolean(key)

    override fun getInt(key: String): Int? = data.getInt(key)

    override fun getDouble(key: String): Double? = data.getDouble(key)

    override fun getFloat(key: String): Float? = data.getFloat(key)

    public override fun toRegion(world: World): Region {
        return Region(getMarkerId(), getMapPoint("min").toLocation(world), getMapPoint("max").toLocation(world))
    }

    override fun getMapPoint(key: String): MapPoint {
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