package me.hugo.thankmas.player.rank

import dev.kezz.miniphrase.MiniPhraseContext
import me.hugo.thankmas.ThankmasPlugin
import me.hugo.thankmas.player.ScoreboardPlayerData
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.luckperms.api.LuckPermsProvider
import org.bukkit.entity.Player
import java.util.*

/**
 * Version of [ScoreboardPlayerData] that automatically adds rank prefixes.
 */
public open class RankedPlayerData(playerUUID: UUID) : ScoreboardPlayerData(playerUUID) {

    context(MiniPhraseContext)
    override fun initializeBoard(title: String?, locale: Locale?, player: Player?): Player {
        val finalPlayer = super.initializeBoard(title, locale, player)

        val translations = ThankmasPlugin.instance().globalTranslations

        playerNameTag = PlayerNameTag(
            playerUUID,
            { viewer, preferredLocale ->
                NamedTextColor.nearestTo(
                    translations.translate(
                        "rank.${getPrimaryGroup(finalPlayer)}.color",
                        preferredLocale ?: viewer.locale()
                    ).color() ?: NamedTextColor.BLACK
                )
            },
            { viewer, preferredLocale ->
                translations.translate(
                    "rank.${getPrimaryGroup(finalPlayer)}.prefix",
                    preferredLocale ?: viewer.locale()
                )
                    .append(Component.space())
            }
        )

        return finalPlayer
    }

    private fun getPrimaryGroup(player: Player): String {
        val api = LuckPermsProvider.get()
        val user = api.getPlayerAdapter(Player::class.java).getUser(player)

        return user.primaryGroup
    }

}