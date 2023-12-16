package me.hugo.thankmas.player

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

/**
 * PlayerData class that provides a player
 * properties to get the bukkit player object.
 */
public open class PaperPlayerData(playerUUID: UUID) : PlayerData(playerUUID) {

    /** @returns the player object from the UUID if online, can be null. */
    public val onlinePlayerOrNull: Player?
        get() = Bukkit.getPlayer(playerUUID)?.takeIf { it.isOnline }

    /** @returns the player object from UUID if online. */
    public val onlinePlayer: Player
        get() {
            val player = onlinePlayerOrNull
            requireNotNull(player) { "Tried to access onlinePlayer while the player is null or offline." }

            return player
        }
}