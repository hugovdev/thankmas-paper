package me.hugo.thankmas.world

import com.infernalsuite.aswm.api.SlimePlugin
import com.infernalsuite.aswm.api.world.SlimeWorld
import com.infernalsuite.aswm.api.world.properties.SlimeProperties
import com.infernalsuite.aswm.api.world.properties.SlimePropertyMap
import me.hugo.thankmas.registry.MapBasedRegistry
import org.bukkit.Bukkit
import org.koin.core.annotation.Single

/** Simple registry for slime worlds in memory. */
@Single
public class SlimeWorldRegistry : MapBasedRegistry<String, SlimeWorld>() {

    public companion object {
        /** Default Save The Kweebec map properties. */
        private val DEFAULT_PROPERTIES = SlimePropertyMap().apply {
            setValue(SlimeProperties.DIFFICULTY, "normal")
            setValue(SlimeProperties.ALLOW_ANIMALS, false)
            setValue(SlimeProperties.ALLOW_MONSTERS, false)
        }
    }

    /** Gets or loads a slime world, should be run asynchronously on runtime. */
    public fun getOrLoad(slimeWorldName: String): SlimeWorld {
        val slimePlugin: SlimePlugin = Bukkit.getPluginManager().getPlugin("SlimeWorldManager") as SlimePlugin

        val slimeWorld = getOrNull(slimeWorldName) ?: slimePlugin.loadWorld(
            slimePlugin.getLoader(""),
            slimeWorldName,
            true,
            DEFAULT_PROPERTIES
        ).also { register(slimeWorldName, it) }

        return slimeWorld
    }

}