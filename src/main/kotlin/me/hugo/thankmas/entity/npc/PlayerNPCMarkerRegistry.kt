package me.hugo.thankmas.entity.npc

import com.google.gson.JsonArray
import com.google.gson.JsonParser
import me.hugo.thankmas.ThankmasPlugin
import me.hugo.thankmas.config.ConfigurationProvider
import me.hugo.thankmas.entity.Hologram
import me.hugo.thankmas.items.TranslatableItem
import me.hugo.thankmas.lang.TranslatedComponent
import me.hugo.thankmas.markers.Marker
import me.hugo.thankmas.player.translate
import me.hugo.thankmas.registry.MapBasedRegistry
import me.hugo.thankmas.world.AnvilWorldRegistry
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
import org.koin.core.annotation.Single
import org.koin.core.component.inject
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

/** Holds all the information for a configured player NPC. */
public data class PlayerNPC(
    public val npc: NPC,
    public val marker: Marker,
    public val hologram: Hologram?
)

/** Spawns all the player NPCs for player_npc markers around [world]. */
@Single
public class PlayerNPCMarkerRegistry(private val world: String) : MapBasedRegistry<String, PlayerNPC>(),
    TranslatedComponent, Listener {

    public companion object {
        public const val LOADING_SKIN_TEXTURE: String =
            "eyJ0aW1lc3RhbXAiOjE1ODc4MjU0NzgwNDcsInByb2ZpbGVJZCI6ImUzYjQ0NWM4NDdmNTQ4ZmI4YzhmYTNmMWY3ZWZiYThlIiwicHJvZmlsZU5hbWUiOiJNaW5pRGlnZ2VyVGVzdCIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2E1ODg4YWEyZDdlMTk5MTczYmEzN2NhNzVjNjhkZTdkN2Y4NjJiMzRhMTNiZTMyNDViZTQ0N2UyZjIyYjI3ZSJ9fX0="
        public const val LOADING_SKIN_SIGNATURE: String =
            "Yt6VmTAUTbpfGQoFneECtoYcbu7jcARAwZu2LYWv3Yf1MJGXv6Bi3i7Pl9P8y1ShB7V1Q2HyA1bce502x1naOKJPzzMJ0jKZfEAKXnzaFop9t9hXzgOq7PaIAM6fsapymYhkkulRIxnJdMrMb2PLRYfo9qiBJG+IEbdj8MTSvWJO10xm7GtpSMmA2Xd0vg5205hsj0OxSdgxf1uuWPyRaXpPZYDUU05/faRixDKti86hlkBs/v0rttU65r1UghkftfjK0sJoPpk9hABvkw4OjXVFb63wcb27KPhIiSHZzTooSxjGNDniauCsF8Je+fhhMebpXeba1R2lZPLhkHwazNgZmTCKbV1M/a8BDHN24HH9okJpQOR9SPCPOJrNbK+LTPsrR06agj+H/yvYq0ZMJTF6IE6C3KJqntPJF1NQvJM0/YegPPtzpbT/7O1cd4JBCVmguhadOFYvrxqCKHcmaYdkyMJtnGub/5sCjJAG7fZadACftwLnmdBZoQRcNKQMubpdUjuzF8g6C03MiZkeNBUgqkfVjXi7DqpmB0ZvTttp34vy7EIBCo3Hfj15779nGs8SoTw9V2zZc+LgiVPjWF6tffjWkgzLq8K2Cndu6RDlWGJWmrztN/X9lIiLdn8GEfSSGY983n0C91x8mkpOKSfAWPnSZd7NuHU5GaoMvyE="
    }

    private val spawnWorld: World
        get() = requireNotNull(Bukkit.getWorld(world))
        { "Tried to spawn player NPCs in world $world but it is not loaded." }

    private val anvilWorldRegistry: AnvilWorldRegistry by inject()
    private val configProvider: ConfigurationProvider by inject()

    init {
        anvilWorldRegistry.getMarkerForType(world, "player_npc").forEach { spawnNPC(it) }
    }

    private fun spawnNPC(marker: Marker) {
        val npcUUID = UUID.randomUUID()
        val npcId = marker.getString("id") ?: npcUUID.toString()

        val npcSkin = marker.getStringList("skin") ?: emptyList()
        val isPlayerSkin = npcSkin.size == 1

        val npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "")
        val npcsConfig =
            configProvider.getOrLoadOrNull("${ThankmasPlugin.instance().configScopes.firstOrNull() ?: "global"}/npcs.yml")

        // If the skin list has two values then its value (0) and signature (1).
        npc.getOrAddTrait(SkinTrait::class.java)?.apply {
            setSkinPersistent(
                npcId,
                if (!isPlayerSkin) npcSkin[1] else npcsConfig?.getString("$npcId.skin.signature")
                    ?: LOADING_SKIN_SIGNATURE,
                if (!isPlayerSkin) npcSkin[0] else npcsConfig?.getString("$npcId.skin.textures") ?: LOADING_SKIN_TEXTURE
            )
        }

        npc.data().apply {
            setPersistent(NPC.Metadata.SHOULD_SAVE, false)
            setPersistent(NPC.Metadata.NAMEPLATE_VISIBLE, false)
            setPersistent(NPC.Metadata.ALWAYS_USE_NAME_HOLOGRAM, true)

            setPersistent("id", npcId)
            setPersistent("use", marker.getString("use"))
        }

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

        var hologram: Hologram? = null

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
                }
            )
        }


        if (getOrNull(npcId) != null) {
            ThankmasPlugin.instance().logger.warning("Key $npcId is duplicated!")
            return
        }

        npc.spawn(marker.location.toLocation(spawnWorld))
        register(npcId, PlayerNPC(npc, marker, hologram))
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

    public fun generateSkins() {
        val npcsConfig =
            configProvider.getOrLoadOrNull("${ThankmasPlugin.instance().configScopes.firstOrNull() ?: "global"}/npcs.yml")
                ?: return

        val dynamicSkins = anvilWorldRegistry.getMarkerForType(world, "player_npc")
            .filter { it.getStringList("skin")?.size == 1 }

        Bukkit.getScheduler().runTaskAsynchronously(ThankmasPlugin.instance(), Runnable {
            val client = HttpClient.newHttpClient()

            val request = HttpRequest.newBuilder()
                .header("Accept", "application/json")
                .timeout(10.seconds.toJavaDuration())

            val playerUUIDs: MutableMap<String, String> = mutableMapOf()

            dynamicSkins.chunked(10).forEachIndexed { index, markerList ->
                request.uri(URI.create("https://api.minecraftservices.com/minecraft/profile/lookup/bulk/byname"))
                    .POST(
                        HttpRequest.BodyPublishers.ofString(
                            JsonArray().also { bodyObject ->
                                markerList.forEach { marker ->
                                    bodyObject.add(marker.getStringList("skin")!!.first())
                                }
                            }.toString()
                        )
                    ).build().let { request ->
                        val response = client.send(request, HttpResponse.BodyHandlers.ofString()).body()
                        JsonParser.parseString(response).asJsonArray.map { it.asJsonObject }
                            .forEach { playerUUIDs[it.get("name").asString] = it.get("id").asString }

                        println("Resolved chunk of uuids $index: $response")
                    }
            }

            playerUUIDs.forEach { (name, uuid) ->
                println("Resolving skin for $name, $uuid")

                request.GET()
                    .uri(URI.create("https://sessionserver.mojang.com/session/minecraft/profile/$uuid?unsigned=false"))
                    .build().let { request ->
                        val response = JsonParser.parseString(
                            client.send(request, HttpResponse.BodyHandlers.ofString()).body()
                        ).asJsonObject

                        val npcId = dynamicSkins.first { it.getStringList("skin")!!.first().equals(name, true) }
                            .getString("id")

                        val texturesProperty = response.get("properties").asJsonArray.first {
                            it.asJsonObject.get("name").asString.equals(
                                "textures",
                                true
                            )
                        }.asJsonObject

                        npcsConfig.set("$npcId.skin.textures", texturesProperty.get("value").asString)
                        npcsConfig.set("$npcId.skin.signature", texturesProperty.get("signature").asString)
                    }
            }

            val configPath = (ThankmasPlugin.instance().configScopes.firstOrNull() ?: "global") + "/npcs.yml"

            npcsConfig.save(Bukkit.getPluginsFolder().resolve(configPath))
        })
    }
}