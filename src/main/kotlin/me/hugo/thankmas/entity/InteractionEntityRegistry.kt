package me.hugo.thankmas.entity

import me.hugo.thankmas.ThankmasPlugin
import me.hugo.thankmas.lang.TranslatedComponent
import me.hugo.thankmas.registry.MapBasedRegistry
import me.hugo.thankmas.world.registry.AnvilWorldRegistry
import org.bukkit.entity.EntityType
import org.bukkit.entity.Interaction
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.koin.core.component.inject
import java.util.*

/** Registers interaction entities in [world] and activates listeners for different callbacks with ids. */
public class InteractionEntityRegistry(
    instance: ThankmasPlugin<*>,
    private val interactionCallbacks: Map<String, (clicker: Player) -> Unit> = mapOf()
) : MapBasedRegistry<UUID, String>(), Listener,
    TranslatedComponent {

    private val anvilWorldRegistry: AnvilWorldRegistry by inject()

    init {
        anvilWorldRegistry.getMarkerForType(instance.worldName, "interaction_entity").forEach {
            val id = it.getString("id") ?: return@forEach

            val width = it.getFloat("width") ?: 1.0f
            val height = it.getFloat("height") ?: 1.0f

            val interactionEntity = it.world.spawnEntity(
                it.location.toLocation(it.world),
                EntityType.INTERACTION,
                CreatureSpawnEvent.SpawnReason.CUSTOM
            ) { interaction ->
                interaction as Interaction

                interaction.interactionWidth = width
                interaction.interactionHeight = height

                interaction.isResponsive = true
            }

            register(interactionEntity.uniqueId, id)
        }
    }

    @EventHandler
    public fun onInteractionClicked(event: PlayerInteractEntityEvent) {
        val interaction = event.rightClicked as? Interaction ?: return
        val id = getOrNull(interaction.uniqueId) ?: return

        interactionCallbacks[id]?.invoke(event.player)
    }
}