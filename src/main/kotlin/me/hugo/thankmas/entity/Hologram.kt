package me.hugo.thankmas.entity

import io.papermc.paper.adventure.PaperAdventure
import me.hugo.thankmas.player.PaperPlayerData
import me.hugo.thankmas.player.PlayerDataManager
import net.kyori.adventure.text.Component
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.world.entity.Display.TextDisplay
import net.minecraft.world.entity.EntityType
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_20_R2.CraftWorld
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer
import org.bukkit.entity.Player

/** TextDisplay that shows different text per player. */
public class Hologram(
    private val location: Location,
    private val textSupplier: (viewer: Player) -> Component,
    private val playerManager: PlayerDataManager<PaperPlayerData>
) {

    private val craftWorld = location.world as CraftWorld
    private val level = craftWorld.handle

    /** Spawns this hologram to [player]. */
    public fun spawnOrRespawn(player: Player) {
        val playerData = playerManager.getPlayerData(player.uniqueId)

        // If the hologram has already spawned for [player], we de-spawn it before spawning it again.
        if (playerData.getHologramIdOrNull(this) != null) remove(player)

        val holographicText = TextDisplay(EntityType.TEXT_DISPLAY, level)
        holographicText.text = PaperAdventure.asVanilla(textSupplier(player))

        // TODO: Custom hologram properties.

        (player as CraftPlayer).handle.connection.send(ClientboundAddEntityPacket(holographicText))
        playerData.addHologram(this, holographicText.id)
    }

    /** Removes this hologram from the [player]'s client. */
    public fun remove(player: Player) {
        val playerData = playerManager.getPlayerData(player.uniqueId)
        val entityId = playerData.getHologramIdOrNull(this)
        requireNotNull(entityId) { "Tried to despawn a hologram from ${player.name}, who wasn't a viewer! " }

        ClientboundRemoveEntitiesPacket(entityId)
    }

}