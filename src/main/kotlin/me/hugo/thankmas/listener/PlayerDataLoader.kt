package me.hugo.thankmas.listener

import me.hugo.thankmas.ThankmasPlugin
import me.hugo.thankmas.lang.TranslatedComponent
import me.hugo.thankmas.player.PaperPlayerData
import me.hugo.thankmas.player.PlayerDataManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.sql.SQLException

/**
 * Loads player data and keeps track of it until the player
 * disconnects from the server!
 */
public class PlayerDataLoader<T : PaperPlayerData<T>>(
    private val instance: ThankmasPlugin,
    private val playerManager: PlayerDataManager<T>
) : Listener, TranslatedComponent {

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onPlayerPreLogin(event: AsyncPlayerPreLoginEvent) {
        if (event.loginResult != AsyncPlayerPreLoginEvent.Result.ALLOWED) return

        val playerUUID = event.uniqueId

        // If there is data from this player in the current session storage
        // something went very wrong!
        if (playerManager.hasPlayerDaya(playerUUID)) {
            event.disallow(
                AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                instance.globalTranslations.translate("general.kick.player_data_loaded")
            )

            return
        }

        // Load player data from the database into the login cache!
        try {
            playerManager.createPlayerData(playerUUID)
        } catch (exception: SQLException) {
            exception.printStackTrace()

            // Something went wrong!
            event.disallow(
                AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                Component.text("Your data could not be loaded!", NamedTextColor.RED)
            )
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        // Player successfully logged in, register the data!
        playerManager.registerPlayerData(player.uniqueId).onPrepared(player)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onPlayerQuit(event: PlayerQuitEvent) {
        val playerId = event.player.uniqueId

        // Player quit, save their data and forget the player!
        playerManager.getPlayerData(playerId).saveSafely { playerManager.removePlayerData(playerId) }
    }
}