package me.hugo.thankmas.listener

import me.hugo.thankmas.config.ConfigurationProvider
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeInstance
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Modifies player attributes on join based on a config file! */
public class PlayerAttributes : Listener, KoinComponent {

    private val configProvider: ConfigurationProvider by inject()

    private val attributesConfig
        get() = configProvider.getOrLoad("hub/attributes.yml")

    @EventHandler
    public fun onPlayerSpawn(event: PlayerJoinEvent) {
        val buffedPlayer = event.player

        buffedPlayer.walkSpeed = 0.5f

        attributesConfig.getKeys(true).forEach { configKey ->
            val attribute = requireNotNull(Attribute.valueOf(configKey.uppercase()))
            { "Tried to modify player attribute $configKey, but doesn't exist!" }

            val value = attributesConfig.getDouble(configKey)

            // Use a different method for walk speed so the fov doesn't change!
            if (attribute == Attribute.GENERIC_MOVEMENT_SPEED) {
                buffedPlayer.walkSpeed = value.toFloat()
                return@forEach
            }

            buffedPlayer.getOrRegisterAttribute(attribute).baseValue = value
        }
    }

    /** Gets [attribute] from this player and registers it if missing. */
    private fun Player.getOrRegisterAttribute(attribute: Attribute): AttributeInstance {
        return getAttribute(attribute) ?: registerAttribute(attribute).let { getAttribute(attribute)!! }
    }

}