package me.hugo.thankmas.player

import dev.kezz.miniphrase.MiniPhraseContext
import fr.mrmicky.fastboard.adventure.FastBoard
import java.util.*

public open class ScoreboardPlayerData(playerUUID: UUID) : PaperPlayerData(playerUUID) {

    private var board: FastBoard? = null

    context(MiniPhraseContext)
    public open fun initializeBoard(title: String? = null) {
        val player = onlinePlayer

        val board = FastBoard(player)
        title?.let { board.updateTitle(player.translate(title)) }

        this.board = board
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

}