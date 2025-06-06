package me.hugo.thankmas.player.rank

import me.hugo.thankmas.ThankmasPlugin
import me.hugo.thankmas.player.PlayerDataManager
import me.hugo.thankmas.player.player
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.event.user.UserDataRecalculateEvent
import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * Listens to any group changes made by LuckPerms and updates
 * the player's name tag and sidebar.
 */
public class PlayerGroupChange<P : RankedPlayerData<P>>(
    private val playerManager: PlayerDataManager<P>,
    /** Predicate that checks if [player] should get their tag updated right now. */
    shouldUpdate: (player: Player) -> Boolean = { true },
    /** Extra actions that run when [player]'s permissions profile changes. */
    extraActions: (player: Player) -> Unit = {}
) {

    init {
        val luckPerms = LuckPermsProvider.get()

        // Player rank changes so we update their name tags!
        luckPerms.eventBus.subscribe(UserDataRecalculateEvent::class.java) { event ->

            val userId = event.user.uniqueId

            Bukkit.getScheduler().getMainThreadExecutor(ThankmasPlugin.instance<ThankmasPlugin<*>>()).execute {
                val onlinePlayer = userId.player() ?: return@execute

                if (!shouldUpdate(onlinePlayer)) return@execute

                playerManager.getPlayerData(userId).playerNameTag?.updateTeamId()
                extraActions(onlinePlayer)
            }
        }
    }

}