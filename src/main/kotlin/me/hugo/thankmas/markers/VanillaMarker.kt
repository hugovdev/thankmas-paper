package me.hugo.thankmas.markers

import me.hugo.thankmas.location.MapPoint
import me.hugo.thankmas.region.WeakRegion
import net.minecraft.nbt.*

public class VanillaMarker(location: MapPoint, worldName: String, public val data: CompoundTag) :
    Marker(location, worldName) {

    override fun getMarkerId(): String = data.getString("name")

    override fun getKeys(): Collection<String> = data.allKeys

    override fun getString(key: String): String? =
        if (data.contains(key, Tag.TAG_STRING.toInt())) data.getString(key) else null

    override fun getBoolean(key: String): Boolean? =
        if (data.contains(key, Tag.TAG_ANY_NUMERIC.toInt())) data.getBoolean(key) else null

    override fun getInt(key: String): Int? =
        if (data.contains(key, Tag.TAG_ANY_NUMERIC.toInt())) data.getInt(key) else null

    override fun getDouble(key: String): Double? =
        if (data.contains(key, Tag.TAG_ANY_NUMERIC.toInt())) data.getDouble(key) else null

    override fun getFloat(key: String): Float? =
        if (data.contains(key, Tag.TAG_ANY_NUMERIC.toInt())) data.getFloat(key) else null

    override fun getStringList(key: String): List<String>? = if (data.contains(key, Tag.TAG_LIST.toInt()))
        data.getList(key, Tag.TAG_STRING.toInt()).map { (it as StringTag).asString } else null

    override fun getIntList(key: String): List<Int>? = if (data.contains(key, Tag.TAG_LIST.toInt()))
        data.getList(key, Tag.TAG_INT.toInt()).map { (it as IntTag).asInt } else null

    override fun getFloatList(key: String): List<Float>? = if (data.contains(key, Tag.TAG_LIST.toInt()))
        data.getList(key, Tag.TAG_FLOAT.toInt()).map { (it as FloatTag).asFloat } else null

    override fun getDoubleList(key: String): List<Double>? = if (data.contains(key, Tag.TAG_LIST.toInt()))
        data.getList(key, Tag.TAG_DOUBLE.toInt()).map { (it as DoubleTag).asDouble } else null

    override fun toRegion(id: String?): WeakRegion =
        WeakRegion(id ?: getMarkerId(), getMapPoint("min"), getMapPoint("max"))

    override fun getMapPoint(key: String): MapPoint {
        val list = requireNotNull(getDoubleList(key))
        { "Tried to get map point with key $key but no data was found." }

        return if (list.size == 3) {
            MapPoint(list[0], list[1], list[2], 0.0f, 0.0f)
        } else MapPoint(
            list[0], list[1], list[2],
            list[3].toFloat(),
            list[4].toFloat()
        )
    }
}