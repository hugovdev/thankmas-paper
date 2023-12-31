package me.hugo.thankmas.player.rank

import dev.kezz.miniphrase.MiniPhraseContext
import me.hugo.thankmas.ThankmasPlugin
import me.hugo.thankmas.player.ScoreboardPlayerData
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.model.group.Group
import org.bukkit.entity.Player
import java.util.*

/**
 * Version of [ScoreboardPlayerData] that automatically adds rank prefixes.
 */
public open class RankedPlayerData(
    playerUUID: UUID,
    private val suffixSupplier: ((viewer: Player, preferredLocale: Locale?) -> Component)? = null,
    private val belowNameSupplier: ((viewer: Player, preferredLocale: Locale?) -> Component)? = null,
) : ScoreboardPlayerData(playerUUID) {

    context(MiniPhraseContext)
    override fun initializeBoard(title: String?, locale: Locale?, player: Player?): Player {
        val finalPlayer = super.initializeBoard(title, locale, player)

        val translations = ThankmasPlugin.instance().globalTranslations

        playerNameTag = PlayerNameTag(
            playerUUID,
            {
                val rankIndex = 99 - (getPrimaryGroupOrNull(finalPlayer)?.weight?.orElse(0) ?: 0)
                "$rankIndex-$playerUUID"
            },
            { viewer, preferredLocale ->
                NamedTextColor.nearestTo(
                    translations.translate(
                        "rank.${getPrimaryGroupName(finalPlayer)}.color",
                        preferredLocale ?: viewer.locale()
                    ).color() ?: NamedTextColor.BLACK
                )
            },
            { viewer, preferredLocale ->
                translations.translate(
                    "rank.${getPrimaryGroupName(finalPlayer)}.prefix",
                    preferredLocale ?: viewer.locale()
                )
                    .append(Component.space())
            },
            suffixSupplier,
            belowNameSupplier
        )

        return finalPlayer
    }

    /** @returns the primary LuckPerms group for [player]. */
    public fun getPrimaryGroupName(player: Player): String {
        val api = LuckPermsProvider.get()
        val user = api.getPlayerAdapter(Player::class.java).getUser(player)

        return user.primaryGroup
    }

    /** @returns the primary LuckPerms group for [player]. */
    private fun getPrimaryGroupOrNull(player: Player): Group? {
        val api = LuckPermsProvider.get()

        return api.groupManager.getGroup(getPrimaryGroupName(player))
    }

    /** @returns the full player name tag with their rank prefix. */
    public fun getNameTag(): Component {
        val finalPlayer = onlinePlayer
        val translations = ThankmasPlugin.instance().globalTranslations
        val locale = finalPlayer.locale()

        val primaryGroup = getPrimaryGroupName(finalPlayer)

        return translations.translate("rank.${primaryGroup}.prefix", locale).append(Component.space()).append(
            Component.text(finalPlayer.name).color(
                NamedTextColor.nearestTo(
                    translations.translate(
                        "rank.${primaryGroup}.color",
                        locale
                    ).color() ?: NamedTextColor.BLACK
                )
            )
        )
    }

}