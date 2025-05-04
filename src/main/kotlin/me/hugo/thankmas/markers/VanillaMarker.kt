package me.hugo.thankmas.markers

import me.hugo.thankmas.location.MapPoint
import me.hugo.thankmas.region.WeakRegion
import net.minecraft.nbt.*
import kotlin.jvm.optionals.getOrNull

public class VanillaMarker(location: MapPoint, worldName: String, public val data: CompoundTag) :
    Marker(location, worldName) {

    override fun getMarkerId(): String = data.getString("name").get()

    override fun getKeys(): Collection<String> = data.keySet()

    override fun getString(key: String): String? = data.getString(key).getOrNull()

    override fun getBoolean(key: String): Boolean? = data.getBoolean(key).getOrNull()

    override fun getInt(key: String): Int? = data.getInt(key).getOrNull()

    override fun getDouble(key: String): Double? = data.getDouble(key).getOrNull()

    override fun getFloat(key: String): Float? = data.getFloat(key).getOrNull()

    override fun getStringList(key: String): List<String>? = data.getList(key).getOrNull()?.map { it.asString().get() }

    override fun getIntList(key: String): List<Int>? = data.getList(key).getOrNull()?.map { it.asInt().get() }

    override fun getFloatList(key: String): List<Float>? = data.getList(key).getOrNull()?.map { it.asFloat().get() }

    override fun getDoubleList(key: String): List<Double>? = data.getList(key).getOrNull()?.map { it.asDouble().get() }

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