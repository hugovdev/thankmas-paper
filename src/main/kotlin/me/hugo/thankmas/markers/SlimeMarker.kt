package me.hugo.thankmas.markers

import com.flowpowered.nbt.CompoundTag
import me.hugo.thankmas.location.MapPoint
import me.hugo.thankmas.region.Region
import org.bukkit.World
import kotlin.jvm.optionals.getOrNull

public class SlimeMarker(location: MapPoint, worldName: String, public val data: CompoundTag) :
    Marker(location, worldName) {

    public override fun getMarkerId(): String {
        return data.getStringValue("name").get()
    }

    override fun getString(key: String): String? = data.getStringValue(key).getOrNull()

    override fun getBoolean(key: String): Boolean? = data.getByteValue(key).getOrNull()?.let { it != 0.toByte() }

    override fun getInt(key: String): Int? = data.getIntValue(key).getOrNull()

    override fun getDouble(key: String): Double? = data.getDoubleValue(key).getOrNull()

    override fun getFloat(key: String): Float? = data.getFloatValue(key).getOrNull()

    override fun getStringList(key: String): List<String>? =
        data.getAsListTag(key)?.getOrNull()?.asStringTagList?.getOrNull()?.value?.map { it.value }

    override fun getIntList(key: String): List<Int>? =
        data.getAsListTag(key)?.getOrNull()?.asIntTagList?.getOrNull()?.value?.map { it.value }

    override fun getFloatList(key: String): List<Float>? =
        data.getAsListTag(key)?.getOrNull()?.asFloatTagList?.getOrNull()?.value?.map { it.value }

    override fun getDoubleList(key: String): List<Double>? =
        data.getAsListTag(key)?.getOrNull()?.asDoubleTagList?.getOrNull()?.value?.map { it.value }

    public override fun toRegion(world: World): Region {
        return Region(getMarkerId(), getMapPoint("min").toLocation(world), getMapPoint("max").toLocation(world))
    }

    public override fun getMapPoint(key: String): MapPoint {
        val coordinateList = requireNotNull(data.getAsListTag(key).getOrNull()?.asDoubleTagList?.getOrNull())
        { "Tried to get map point with key $key but no data was found." }

        val list = coordinateList.value

        return if (list.size == 3) {
            MapPoint(list[0].value, list[1].value, list[2].value, 0.0f, 0.0f)
        } else MapPoint(
            list[0].value, list[1].value, list[2].value,
            list[3].value.toFloat(),
            list[4].value.toFloat()
        )
    }
}