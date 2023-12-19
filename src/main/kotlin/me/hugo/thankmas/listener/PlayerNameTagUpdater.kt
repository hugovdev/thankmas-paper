package me.hugo.thankmas.listener

import me.hugo.thankmas.player.PlayerDataManager
import me.hugo.thankmas.player.ScoreboardPlayerData
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

/**
 * Updates player name tags on join and leave.
 */
public class PlayerNameTagUpdater<T : ScoreboardPlayerData>(
    public val playerManager: PlayerDataManager<T>
) : Listener {

    @EventHandler
    private fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        val playerData = playerManager.getPlayerData(player.uniqueId)

        Bukkit.getOnlinePlayers().forEach {
            playerData.playerNameTag?.apply(it)

            if (it == player) return@forEach
            playerManager.getPlayerDataOrNull(it.uniqueId)?.playerNameTag?.apply(player)
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private fun onLeave(event: PlayerQuitEvent) {
        val player = event.player

        val playerNameTag = playerManager.getPlayerData(player.uniqueId).playerNameTag ?: return
        Bukkit.getOnlinePlayers().forEach { playerNameTag.remove(it.scoreboard) }
    }

}