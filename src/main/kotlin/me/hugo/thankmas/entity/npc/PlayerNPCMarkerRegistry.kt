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

/** Holds all the information for a configured player NPC. */
public data class PlayerNPC(
    public val npc: NPC,
    public val marker: Marker,
    public val hologram: Hologram<*>?
)

/** Spawns all the player NPCs for player_npc markers around [world]. */
public class PlayerNPCMarkerRegistry<P : PaperPlayerData<P>>(
    private val world: String,
    private val playerManager: PlayerDataManager<P>
) : MapBasedRegistry<String, PlayerNPC>(), TranslatedComponent, Listener {

    private val spawnWorld: World
        get() = requireNotNull(Bukkit.getWorld(world))
        { "Tried to spawn player NPCs in world $world but it is not loaded." }

    private val markerRegistry: MarkerRegistry by inject()
    private val configProvider: ConfigurationProvider by inject()

    private var skinFetches: Int = 0
    private val pendingSkinNPCs: MutableSet<PlayerNPC> = mutableSetOf()

    init {
        markerRegistry.getMarkerForType("player_npc").forEach { spawnNPC(it) }

        // Apply 20 player NPC skins per minute! (Mojang API rate limits)
        object : BukkitRunnable() {
            override fun run() {
                val skins = pendingSkinNPCs.take(20)

                if (skins.isEmpty() || skinFetches >= 15) {
                    cancel()
                    return
                }

                skins.forEach {
                    it.npc.getOrAddTrait(SkinTrait::class.java).skinName = it.marker.getStringList("skin")!!.first()

                    pendingSkinNPCs -= it
                }
            }
        }.runTaskTimer(ThankmasPlugin.instance(), 0L, 20 * 60)
    }

    private fun spawnNPC(marker: Marker) {
        val npcUUID = UUID.randomUUID()
        val npcId = marker.getString("id") ?: npcUUID.toString()

        val npcSkin = marker.getStringList("skin") ?: emptyList()
        val isPlayerSkin = npcSkin.size == 1

        val npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, npcUUID.toString())

        // If the skin list has two values then its value (0) and signature (1).
        if (!isPlayerSkin) {
            npc.getOrAddTrait(SkinTrait::class.java)?.apply {
                setSkinPersistent(
                    npcId,
                    npcSkin[1],
                    npcSkin[0]
                )
            }
        }

        val data = npc.data()

        data.setPersistent(NPC.Metadata.SHOULD_SAVE, false)
        data.setPersistent(NPC.Metadata.NAMEPLATE_VISIBLE, false)
        data.setPersistent(NPC.Metadata.ALWAYS_USE_NAME_HOLOGRAM, true)

        data.setPersistent("id", npcId)
        data.setPersistent("use", marker.getString("use"))

        val npcsConfig =
            configProvider.getOrLoadOrNull("${ThankmasPlugin.instance().configScopes.firstOrNull() ?: "global"}/npcs.yml")

        npcsConfig?.let {
            npc.getOrAddTrait(Equipment::class.java).apply {
                Equipment.EquipmentSlot.entries.forEach { slot ->
                    if (it.contains("$npcId.equipment.${slot.name.lowercase()}")) {
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
                marker.location.toLocation(spawnWorld).add(0.0, marker.getDouble("hologram_offset") ?: 1.88, 0.0),
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


        if (getOrNull(npcId) != null) {
            ThankmasPlugin.instance().logger.warning("Key $npcId is duplicated!")
            return
        }

        val playerNPC = PlayerNPC(npc, marker, hologram)

        npc.spawn(marker.location.toLocation(spawnWorld))
        register(npcId, playerNPC)

        if (isPlayerSkin) pendingSkinNPCs.add(playerNPC)
    }

    @EventHandler
    private fun onNpcSpawn(event: NPCLinkToPlayerEvent) {
        getOrNull(event.npc.data().get("id"))?.hologram?.let {
            Bukkit.getScheduler().getMainThreadExecutor(ThankmasPlugin.instance()).execute {
                it.spawnOrUpdate(event.player)
            }
        }
    }

    @EventHandler
    private fun onNpcDespawn(event: NPCUnlinkFromPlayerEvent) {
        if (!event.player.isOnline) return

        getOrNull(event.npc.data().get("id"))?.hologram?.let {
            Bukkit.getScheduler().getMainThreadExecutor(ThankmasPlugin.instance()).execute {
                it.remove(event.player)
            }
        }
    }
}