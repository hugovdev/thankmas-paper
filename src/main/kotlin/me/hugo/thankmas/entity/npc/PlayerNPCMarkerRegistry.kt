package me.hugo.thankmas.entity.npc

import me.hugo.thankmas.ThankmasPlugin
import me.hugo.thankmas.config.ConfigurationProvider
import me.hugo.thankmas.entity.Hologram
import me.hugo.thankmas.items.TranslatableItem
import me.hugo.thankmas.lang.TranslatedComponent
import me.hugo.thankmas.markers.Marker
import me.hugo.thankmas.markers.registry.MarkerRegistry
import me.hugo.thankmas.player.PaperPlayerData
import me.hugo.thankmas.player.PlayerDataManager
import me.hugo.thankmas.player.translate
import me.hugo.thankmas.registry.MapBasedRegistry
import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.event.NPCLinkToPlayerEvent
import net.citizensnpcs.api.event.NPCUnlinkFromPlayerEvent
import net.citizensnpcs.api.npc.NPC
import net.citizensnpcs.api.trait.trait.Equipment
import net.citizensnpcs.trait.CurrentLocation
import net.citizensnpcs.trait.LookClose
import net.citizensnpcs.trait.SkinTrait
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Display
import org.bukkit.entity.EntityType
import org.bukkit.entity.TextDisplay
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.scheduler.BukkitRunnable
import org.koin.core.component.inject
import java.util.*

/** Spawns all the player NPCs for player_npc markers around [world]. */
public class PlayerNPCMarkerRegistry<P : PaperPlayerData>(
    private val world: String,
    private val playerManager: PlayerDataManager<P>,
    private val spawnWorld: World = requireNotNull(Bukkit.getWorld(world))
    { "Tried to spawn player NPCs in world $world but it is not loaded." }
) : MapBasedRegistry<String, Triple<NPC, Marker, Hologram<*>?>>(), TranslatedComponent, Listener {

    private val markerRegistry: MarkerRegistry by inject()
    private val configProvider: ConfigurationProvider by inject()

    init {
        markerRegistry.getMarkerForType("player_npc", world).forEach { marker ->
            val npcId = marker.getString("id") ?: UUID.randomUUID().toString()

            val npcSkin = marker.getStringList("skin") ?: emptyList()

            val npc = CitizensAPI.getNPCRegistry()
                .createNPC(EntityType.PLAYER, if (npcSkin.size == 1) npcSkin.first() else "")

            // If the skin list has two values then its value (0) and signature (1).
            if (npcSkin.size == 2) {
                npc.getOrAddTrait(SkinTrait::class.java)?.apply {
                    setSkinPersistent(
                        npcId,
                        npcSkin[1],
                        npcSkin[0]
                    )
                }
            }

            npc.data().setPersistent(NPC.Metadata.SHOULD_SAVE, false)
            npc.data().setPersistent(NPC.Metadata.NAMEPLATE_VISIBLE, false)
            npc.data().setPersistent(NPC.Metadata.ALWAYS_USE_NAME_HOLOGRAM, true)

            npc.data().setPersistent("id", npcId)
            npc.data().setPersistent("use", marker.getString("use"))

            val npcsConfig =
                configProvider.getOrLoadOrNull("${ThankmasPlugin.instance().configScopes.firstOrNull() ?: "global"}/npcs.yml")

            npcsConfig?.let {
                npc.getOrAddTrait(Equipment::class.java).apply {
                    Equipment.EquipmentSlot.entries.forEach { slot ->
                        if (it.getConfigurationSection("$npcId.equipment.${slot.name.lowercase()}") != null) {
                            set(slot, TranslatableItem(it, "$npcId.equipment.${slot.name.lowercase()}").getBaseItem())
                        }
                    }
                }
            }

            npc.getOrAddTrait(CurrentLocation::class.java)
            npc.getOrAddTrait(LookClose::class.java).apply { lookClose(marker.getBoolean("look_close") ?: false) }

            var hologram: Hologram<*>? = null

            marker.getString("text")?.let { textKey ->
                hologram = Hologram(
                    marker.location.toLocation(spawnWorld).add(0.0, marker.getDouble("hologram_offset") ?: 1.85, 0.0),
                    propertiesSupplier = { _, _ ->
                        Hologram.HologramProperties(
                            Display.Billboard.CENTER,
                            textAlignment = TextDisplay.TextAlignment.CENTER
                        )
                    },
                    textSupplier = { viewer, locale ->
                        viewer.translate(textKey, locale) {
                            marker.getKeys().forEach {
                                parsed(it, marker.getString(it))
                            }
                        }
                    },
                    playerManager
                )
            }

            npc.spawn(marker.location.toLocation(spawnWorld))
            register(npcId, Triple(npc, marker, hologram))
        }
    }

    @EventHandler
    private fun onNpcSpawn(event: NPCLinkToPlayerEvent) {
        object : BukkitRunnable() {
            override fun run() {
                getOrNull(event.npc.data().get("id"))?.third?.spawnOrUpdate(event.player)
            }
        }.runTask(ThankmasPlugin.instance())
    }

    @EventHandler
    private fun onNpcDespawn(event: NPCUnlinkFromPlayerEvent) {
        if (!event.player.isOnline) return

        object : BukkitRunnable() {
            override fun run() {
                getOrNull(event.npc.data().get("id"))?.third?.remove(event.player)
            }
        }.runTask(ThankmasPlugin.instance())
    }
}