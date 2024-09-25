package me.hugo.thankmas.player

import dev.kezz.miniphrase.MiniPhraseContext
import fr.mrmicky.fastboard.adventure.FastBoard
import io.papermc.paper.scoreboard.numbers.NumberFormat
import me.hugo.thankmas.scoreboard.getOrCreateObjective
import me.hugo.thankmas.scoreboard.getOrCreateTeam
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Scoreboard
import java.util.*

/** Player data class with scoreboard functionality. */
public open class ScoreboardPlayerData<P : ScoreboardPlayerData<P>>(
    playerUUID: UUID,
    playerDataManager: PlayerDataManager<P>
) :
    PaperPlayerData<P>(playerUUID, playerDataManager) {

    private var board: FastBoard? = null
    public var lastBoardId: String? = null

    public var playerNameTag: PlayerNameTag? = null
        set(tag) {
            field = tag

            if (tag == null) return
            Bukkit.getOnlinePlayers().forEach { tag.apply(it) }
        }

    context(MiniPhraseContext)
    public open fun initializeBoard(title: String? = null, locale: Locale? = null, player: Player? = null): Player {
        val finalPlayer = player ?: onlinePlayer

        finalPlayer.scoreboard = Bukkit.getScoreboardManager().newScoreboard

        val board = FastBoard(finalPlayer)
        title?.let { board.updateTitle(miniPhrase.translate(title, locale ?: finalPlayer.locale())) }

        this.board = board

        return finalPlayer
    }

    /** @returns the player's FastBoard instance, can be null. */
    public fun getBoardOrNull(): FastBoard? {
        return board
    }

    /** @returns the player's FastBoard instance. */
    public fun getBoard(): FastBoard {
        val board = getBoardOrNull()
        requireNotNull(board) { "Tried to fetch a player's fast board while its null." }

        return board
    }

    public class PlayerNameTag(
        private val owner: UUID,
        private val teamIdSupplier: () -> String,
        private val namedTextColor: ((viewer: Player, preferredLocale: Locale?) -> NamedTextColor)? = null,
        private val prefixSupplier: ((viewer: Player, preferredLocale: Locale?) -> Component)? = null,
        private val suffixSupplier: ((viewer: Player, preferredLocale: Locale?) -> Component)? = null,
        private val belowNameSupplier: ((viewer: Player, preferredLocale: Locale?) -> Component)? = null
    ) {

        public companion object {
            /** Identification for the objective displayed below the player's name. */
            public const val BELOW_NAME_OBJECTIVE: String = "below_name"
        }

        /** Identification of the team that will be used for this name tag. */
        private var teamId = teamIdSupplier()

        /** Adds this player name tag to [viewer]'s scoreboard. */
        public fun apply(viewer: Player, preferredLocale: Locale? = null) {
            val playerOwner = owner.player() ?: return

            val team = viewer.scoreboard.getOrCreateTeam(teamId).apply {
                prefix(prefixSupplier?.let { it(viewer, preferredLocale) })
                suffix(suffixSupplier?.let { it(viewer, preferredLocale) })
                color(namedTextColor?.let { it(viewer, preferredLocale) })
            }

            val entry = playerOwner.name
            if (!team.hasEntry(entry)) team.addEntry(entry)

            val belowNameSupplier = belowNameSupplier ?: return

            viewer.scoreboard.getOrCreateObjective(owner.toString(), Criteria.DUMMY, null, {
                it.displayName(null)
                it.displaySlot = DisplaySlot.BELOW_NAME
            }) {
                it.numberFormat(NumberFormat.fixed(belowNameSupplier(viewer, preferredLocale)))
            }
        }

        /** Uses the [teamIdSupplier] to revalidate the team id. */
        public fun updateTeamId(updateTagGlobally: Boolean = true) {
            val oldTeamId = teamId
            teamId = teamIdSupplier()

            Bukkit.getOnlinePlayers().forEach {
                it.scoreboard.getTeam(oldTeamId)?.unregister()

                if (updateTagGlobally) apply(it)
            }
        }

        /** Updates the player name tag for everyone. */
        public fun updateForAll() {
            Bukkit.getOnlinePlayers().forEach { apply(it) }
        }

        /** Removes this player name tag from [scoreboard]. */
        public fun remove(scoreboard: Scoreboard) {
            scoreboard.getTeam(teamId)?.unregister()
            scoreboard.getObjective(owner.toString())?.unregister()
        }
    }

    override fun setLocale(newLocale: Locale) {
        super.setLocale(newLocale)

        val player = onlinePlayer
        playerDataManager.getAllPlayerData().forEach { it.playerNameTag?.apply(player, newLocale) }
    }

    context(MiniPhraseContext)
    override fun onPrepared(player: Player) {
        super.onPrepared(player)

        // Initialize the scoreboard and send welcome message!
        initializeBoard("scoreboard.title")

        // Apply player nametags!
        playerDataManager.getAllPlayerData().forEach {
            if (it.playerUUID == playerUUID) return@forEach
            it.playerNameTag?.apply(player)
        }
    }

    override fun onSave() {
        super.onSave()
        playerNameTag?.let { Bukkit.getOnlinePlayers().forEach { player -> it.remove(player.scoreboard) } }
    }

}