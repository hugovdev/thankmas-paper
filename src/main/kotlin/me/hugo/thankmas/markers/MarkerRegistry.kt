package me.hugo.thankmas.markers

import com.google.common.collect.HashMultimap
import me.hugo.thankmas.ThankmasPlugin
import me.hugo.thankmas.location.MapPoint
import org.bukkit.Bukkit
import org.jglrxavpok.hephaistos.mca.RegionFile
import org.jglrxavpok.hephaistos.nbt.NBTCompound
import org.jglrxavpok.hephaistos.nbt.NBTDouble
import org.koin.core.annotation.Single
import java.io.RandomAccessFile

/** Provides helper functions to load markers on a world. */
@Single
public class MarkerRegistry {

    // world -> marker name -> marker object
    private val loadedMarkers: MutableMap<String, HashMultimap<String, Marker>> = mutableMapOf()

    /** Loads all the markers in the world with name [worldName]. */
    public fun loadWorldMarkers(worldName: String = "world") {
        val startTime = System.currentTimeMillis()
        val logger = ThankmasPlugin.instance().logger

        logger.info("[Markers] [$worldName] Loading markers for world $worldName...")

        val regionPath = Bukkit.getWorldContainer().resolve(worldName).resolve("entities")
        val regionFiles = regionPath.listFiles()?.map { it.name }?.filter { it.endsWith(".mca") } ?: emptyList()

        // Get all the region file names in the entities folder.
        val regionFileNames: List<Pair<Int, Int>> = regionFiles.mapNotNull {
            val nameSplit = it.split(".")

            if (nameSplit.size == 4) {
                Pair(nameSplit[1].toInt(), nameSplit[2].toInt())
            } else null
        }

        regionFileNames.forEach {
            logger.info("[Markers] [$worldName] Loading markers on region ${it.first}, ${it.second}...")

            val regionFile = RegionFile(
                RandomAccessFile(regionPath.resolve(RegionFile.createFileName(it.first, it.second)), "r"),
                it.first,
                it.second
            )

            for (x in 0..<32) {
                for (z in 0..<32) {
                    val chunkData = regionFile.getChunkData(it.first * 32 + x, it.second * 32 + z) ?: continue
                    val entityList = chunkData.getList<NBTCompound>("Entities") ?: continue

                    entityList.forEach entities@{ entityData ->
                        // Entities with no type or non-markers are ignored!
                        val entityId = entityData.getString("id") ?: return@entities
                        if (entityId != "minecraft:marker") return@entities

                        // Empty data compound, we return!
                        val markerData = entityData.getCompound("data") ?: return@entities

                        // No marker name specified, we return!
                        val markerName = markerData.getString("name") ?: return@entities

                        // Marker has no defined location somehow!<
                        val markerLocation = entityData.getList<NBTDouble>("Pos") ?: return@entities

                        // Save the marker and entityData's data!
                        loadedMarkers.computeIfAbsent(worldName) { HashMultimap.create() }
                            .put(
                                markerName, Marker(
                                    MapPoint(
                                        markerLocation[0].value,
                                        markerLocation[1].value,
                                        markerLocation[2].value,
                                        markerData.getFloat("yaw") ?: 0.0f,
                                        markerData.getFloat("pitch") ?: 0.0f
                                    ),
                                    markerData
                                )
                            )
                    }
                }
            }

            regionFile.close()
        }

        logger.info("[Markers] [$worldName] Loaded ${loadedMarkers.entries.sumOf { it.value.size() }} markers in ${System.currentTimeMillis() - startTime}ms!")
    }

    /** Returns every registered marker of [markerId] type for [world]. */
    public fun getMarkerForType(markerId: String, world: String = "world"): Set<Marker> {
        return getMarkers(world).get(markerId)
    }

    /** Returns every registered marker for [world]. */
    public fun getMarkers(world: String = "world"): HashMultimap<String, Marker> {
        return loadedMarkers[world] ?: HashMultimap.create(0, 0)
    }

}