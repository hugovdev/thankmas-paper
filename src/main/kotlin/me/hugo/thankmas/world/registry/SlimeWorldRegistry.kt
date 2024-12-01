package me.hugo.thankmas.world.registry

import com.infernalsuite.aswm.api.AdvancedSlimePaperAPI
import com.infernalsuite.aswm.api.world.SlimeWorld
import com.infernalsuite.aswm.api.world.properties.SlimeProperties
import com.infernalsuite.aswm.api.world.properties.SlimePropertyMap
import com.infernalsuite.aswm.loaders.file.FileLoader
import me.hugo.thankmas.ThankmasPlugin
import me.hugo.thankmas.location.MapPoint
import me.hugo.thankmas.markers.SlimeMarker
import me.hugo.thankmas.world.WorldRegistry
import org.bukkit.Bukkit
import org.koin.core.annotation.Single
import java.io.File
import kotlin.jvm.optionals.getOrNull

@Single
public class SlimeWorldRegistry : WorldRegistry<SlimeWorld>() {

    /** Directory where slime worlds are saved. */
    public val slimeWorldContainer: File = Bukkit.getWorldContainer().resolve("slime_worlds")

    /** Default slime loader used for slime worlds. */
    public val defaultSlimeLoader: FileLoader = FileLoader(slimeWorldContainer)

    public companion object {
        /** Default Save The Kweebec map properties. */
        private val DEFAULT_PROPERTIES = SlimePropertyMap().apply {
            setValue(SlimeProperties.DIFFICULTY, "normal")
            setValue(SlimeProperties.ALLOW_ANIMALS, false)
            setValue(SlimeProperties.ALLOW_MONSTERS, false)
        }
    }

    /** Gets or loads a slime world, should be run asynchronously on runtime. */
    public fun getOrLoad(slimeWorldName: String, properties: SlimePropertyMap = DEFAULT_PROPERTIES): SlimeWorld {
        val slimePaperAPI = AdvancedSlimePaperAPI.instance()

        val slimeWorld = getOrNull(slimeWorldName) ?: slimePaperAPI.readWorld(
            defaultSlimeLoader,
            slimeWorldName,
            true,
            properties
        ).also { register(slimeWorldName, it) }

        return slimeWorld
    }

    override fun getOrLoadWithMarkers(key: String): SlimeWorld {
        val startTime = System.currentTimeMillis()
        val logger = ThankmasPlugin.instance().logger

        logger.info("[Markers] [$key] Loading markers for slime world $key...")

        val slimeWorld = getOrLoad(key)

        slimeWorld.chunkStorage.forEach {
            it.entities.forEach entities@{ entityData ->
                // Entities with no type or non-markers are ignored!
                val entityId = entityData.getStringValue("id").getOrNull() ?: return@entities
                if (entityId != "minecraft:marker") return@entities

                // Empty data compound, we return!
                val markerData = entityData.getAsCompoundTag("data").getOrNull() ?: return@entities

                // Marker has no defined location somehow!<
                val markerLocation = entityData.getAsListTag("Pos").getOrNull()
                    ?.asDoubleTagList?.getOrNull()?.value ?: return@entities

                // Save the marker and entityData's data!
                saveMarker(
                    key, SlimeMarker(
                        MapPoint(
                            markerLocation[0].value,
                            markerLocation[1].value,
                            markerLocation[2].value,
                            markerData.getFloatValue("yaw")?.getOrNull() ?: 0.0f,
                            markerData.getFloatValue("pitch")?.getOrNull() ?: 0.0f
                        ),
                        key,
                        markerData
                    )
                )
            }
        }

        logger.info("[Markers] [$key] Loaded ${getMarkerCount(key)} markers in ${System.currentTimeMillis() - startTime}ms!")

        return slimeWorld
    }

    // Slime worlds need to be loaded to be able to access markers.
    override fun loadMarkers(key: String) {
        getOrLoadWithMarkers(key)
    }

}