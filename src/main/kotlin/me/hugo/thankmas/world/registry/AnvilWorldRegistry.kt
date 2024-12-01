package me.hugo.thankmas.world.registry

import me.hugo.thankmas.ThankmasPlugin
import me.hugo.thankmas.location.MapPoint
import me.hugo.thankmas.markers.AnvilMarker
import me.hugo.thankmas.world.WorldRegistry
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.WorldCreator
import org.jglrxavpok.hephaistos.mca.RegionFile
import org.jglrxavpok.hephaistos.nbt.NBTCompound
import org.jglrxavpok.hephaistos.nbt.NBTDouble
import org.koin.core.annotation.Single
import java.io.RandomAccessFile

@Single
public class AnvilWorldRegistry : WorldRegistry<World>() {

    /** Gets or loads a slime world, should be run asynchronously on runtime. */
    public fun getOrLoad(key: String): World = requireNotNull(Bukkit.getWorld(key) ?: WorldCreator(key).createWorld())

    override fun getOrLoadWithMarkers(key: String): World {
        return getOrLoad(key).also { loadMarkers(key) }
    }

    override fun loadMarkers(key: String) {
        val startTime = System.currentTimeMillis()
        val logger = ThankmasPlugin.instance().logger

        logger.info("[Markers] [$key] Loading markers for anvil world $key...")

        val regionPath = Bukkit.getWorldContainer().resolve(key).resolve("entities")
        val regionFiles = regionPath.listFiles()?.map { it.name }?.filter { it.endsWith(".mca") } ?: emptyList()

        // Get all the region file names in the entities folder.
        val regionFileNames: List<Pair<Int, Int>> = regionFiles.mapNotNull {
            val nameSplit = it.split(".")

            if (nameSplit.size == 4) {
                Pair(nameSplit[1].toInt(), nameSplit[2].toInt())
            } else null
        }

        regionFileNames.forEach {
            logger.info("[Markers] [$key] Loading markers on region ${it.first}, ${it.second}...")

            val regionFile = RegionFile(
                RandomAccessFile(regionPath.resolve(RegionFile.createFileName(it.first, it.second)), "rw"),
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

                        // Marker has no defined location somehow!<
                        val markerLocation = entityData.getList<NBTDouble>("Pos") ?: return@entities

                        // Save the marker and entityData's data!
                        saveMarker(
                            key, AnvilMarker(
                                MapPoint(
                                    markerLocation[0].value,
                                    markerLocation[1].value,
                                    markerLocation[2].value,
                                    markerData.getFloat("yaw") ?: 0.0f,
                                    markerData.getFloat("pitch") ?: 0.0f
                                ),
                                key,
                                markerData
                            )
                        )
                    }
                }
            }

            regionFile.close()
        }

        logger.info("[Markers] [$key] Loaded ${getMarkerCount(key)} markers in ${System.currentTimeMillis() - startTime}ms!")
    }
}