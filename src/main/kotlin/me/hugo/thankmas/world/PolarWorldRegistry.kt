package me.hugo.thankmas.world

import dev.emortal.paperpolar.PolarReader
import dev.emortal.paperpolar.PolarWorld
import me.hugo.thankmas.ThankmasPlugin
import me.hugo.thankmas.location.MapPoint
import me.hugo.thankmas.markers.VanillaMarker
import net.minecraft.nbt.NbtAccounter
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.Tag
import org.bukkit.Bukkit
import org.koin.core.annotation.Single
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.nio.file.Files

@Single
public class PolarWorldRegistry : WorldRegistry<PolarWorld>() {

    /** Directory where polar worlds are saved. */
    public val polarWorldContainer: File = Bukkit.getWorldContainer().resolve("polar_worlds")

    /** Gets or loads a polar world, should be run asynchronously on runtime. */
    public fun getOrLoad(key: String): PolarWorld {
        val polarFile = polarWorldContainer.resolve("$key.polar").toPath()

        try {
            return PolarReader.read(Files.readAllBytes(polarFile))
        } catch (exception: IOException) {
            throw RuntimeException(exception)
        }
    }

    override fun getOrLoadWithMarkers(key: String): PolarWorld {
        val startTime = System.currentTimeMillis()
        val logger = ThankmasPlugin.instance().logger

        logger.info("[Markers] [$key] Loading markers for polar world $key...")

        val polarWorld = getOrLoad(key)

        val entities = polarWorld.chunks().flatMap { it.entities }

        entities.forEach {
            val compoundTag = NbtIo.readCompressed(ByteArrayInputStream(it.bytes), NbtAccounter.unlimitedHeap())

            // Entities with no type or non-markers are ignored!
            val entityId = compoundTag.getString("id") ?: return@forEach
            if (entityId != "minecraft:marker") return@forEach

            // Empty data compound, we return!
            val markerData = compoundTag.getCompound("data") ?: return@forEach

            // Marker has no defined location somehow!
            val markerLocation = compoundTag.getList("Pos", Tag.TAG_DOUBLE.toInt()) ?: return@forEach

            // Save the marker and entityData's data!
            saveMarker(
                key, VanillaMarker(
                    MapPoint(
                        markerLocation.getDouble(0),
                        markerLocation.getDouble(1),
                        markerLocation.getDouble(2),
                        markerData.getFloat("yaw"),
                        markerData.getFloat("pitch"),
                    ),
                    key,
                    markerData
                )
            )
        }

        logger.info("[Markers] [$key] Loaded ${getMarkerCount(key)} markers in ${System.currentTimeMillis() - startTime}ms!")

        return polarWorld
    }

    // Polar worlds need to be loaded to be able to access markers.
    override fun loadMarkers(key: String) {
        getOrLoadWithMarkers(key)
    }
}