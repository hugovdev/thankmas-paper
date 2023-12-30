package me.hugo.thankmas.player

import dev.kezz.miniphrase.MiniPhraseContext
import fr.mrmicky.fastboard.adventure.FastBoard
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

public open class ScoreboardPlayerData(playerUUID: UUID) : PaperPlayerData(playerUUID) {

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

        /** Identification of the team that will be used for this name tag. */
        private var teamId = teamIdSupplier()

        /** Adds this player name tag to [viewer]'s scoreboard. */
        public fun apply(viewer: Player, preferredLocale: Locale? = null) {
            val playerOwner = owner.player() ?: return

            val team = viewer.scoreboard.getOrCreateTeam(teamId)

            team.prefix(prefixSupplier?.let { it(viewer, preferredLocale) })
            team.suffix(suffixSupplier?.let { it(viewer, preferredLocale) })
            team.color(namedTextColor?.let { it(viewer, preferredLocale) })

            val entry = playerOwner.name
            if (!team.hasEntry(entry)) team.addEntry(entry)

            val belowNameSupplier = belowNameSupplier ?: return

            val belowName = viewer.scoreboard.getOrCreateObjective(teamId, Criteria.DUMMY, Component.text("below_name")) {
                it.displaySlot = DisplaySlot.BELOW_NAME
                it.displayName(null)
                // TODO: Set the default number format to blank.
            }

            // TODO: Custom text will be set as score with "fixed" number format per player.
            // belowName.getScore(playerOwner.name)
            // TODO: Set Number Format to fixed whenever its API-ready.
        }

        /** Uses the [teamIdSupplier] to revalidate the team id. */
        public fun updateTeamId(updateTagGlobally: Boolean = true) {
            Bukkit.getOnlinePlayers().forEach {
                it.scoreboard.getTeam(teamId)?.unregister()
                teamId = teamIdSupplier()

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
            scoreboard.getObjective(teamId)?.unregister()
        }

    }

}