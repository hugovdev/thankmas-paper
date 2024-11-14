package me.hugo.thankmas.markers

import me.hugo.thankmas.location.MapPoint
import me.hugo.thankmas.region.Region
import org.bukkit.World
import org.jglrxavpok.hephaistos.nbt.*

public class VanillaMarker(location: MapPoint, worldName: String, public val data: NBTCompound) :
    Marker(location, worldName) {

    public override fun getMarkerId(): String {
        return data.getString("name")!!
    }

    override fun getKeys(): Collection<String> = data.keys

    override fun getString(key: String): String? = data.getString(key)

    override fun getBoolean(key: String): Boolean? = data.getBoolean(key)

    override fun getInt(key: String): Int? = data.getInt(key)

    override fun getDouble(key: String): Double? = data.getDouble(key)

    override fun getFloat(key: String): Float? = data.getFloat(key)

    override fun getStringList(key: String): List<String>? = data.getList<NBTString>(key)?.value?.map { it.value }

    override fun getIntList(key: String): List<Int>? = data.getList<NBTInt>(key)?.value?.map { it.value }

    override fun getFloatList(key: String): List<Float>? = data.getList<NBTFloat>(key)?.value?.map { it.value }

    override fun getDoubleList(key: String): List<Double>? = data.getList<NBTDouble>(key)?.value?.map { it.value }

    public override fun toRegion(world: World, id: String?): Region {
        return Region(id ?: getMarkerId(), getMapPoint("min").toLocation(world), getMapPoint("max").toLocation(world))
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