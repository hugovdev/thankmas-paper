package me.hugo.thankmas.player

import fr.mrmicky.fastboard.adventure.FastBoard
import org.bukkit.entity.Player
import java.util.UUID

public open class ScoreboardPlayerData(playerUUID: UUID) : PlayerData(playerUUID) {

    private var board: FastBoard? = null

    public open fun initializeBoard(player: Player) {
        board = FastBoard(player)
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