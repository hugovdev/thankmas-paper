package me.hugo.thankmas.world

import com.infernalsuite.aswm.api.AdvancedSlimePaperAPI
import com.infernalsuite.aswm.api.world.SlimeWorld
import com.infernalsuite.aswm.api.world.properties.SlimeProperties
import com.infernalsuite.aswm.api.world.properties.SlimePropertyMap
import com.infernalsuite.aswm.loaders.file.FileLoader
import me.hugo.thankmas.registry.MapBasedRegistry
import org.bukkit.Bukkit
import org.koin.core.annotation.Single
import java.io.File

/** Simple registry for slime worlds in memory. */
@Single
public class SlimeWorldRegistry : MapBasedRegistry<String, SlimeWorld>() {

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

}