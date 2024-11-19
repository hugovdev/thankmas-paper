package me.hugo.thankmas.player.rank

import me.hugo.thankmas.ThankmasPlugin
import me.hugo.thankmas.player.PlayerDataManager
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
public open class RankedPlayerData<P : RankedPlayerData<P>>(
    playerUUID: UUID,
    playerDataManager: PlayerDataManager<P>,
    private val rankedNametags: Boolean = true,
    protected val suffixSupplier: ((viewer: Player, preferredLocale: Locale?) -> Component)? = null,
    protected val belowNameSupplier: ((viewer: Player, preferredLocale: Locale?) -> Component)? = null,
) : ScoreboardPlayerData<P>(playerUUID, playerDataManager) {

    override fun initializeBoard(title: String?, locale: Locale?, player: Player?): Player {
        val finalPlayer = super.initializeBoard(title, locale, player)

        // Setup player nametags to show their rank!
        if (rankedNametags) {
            playerNameTag = PlayerNameTag(
                playerUUID,
                {
                    // Order players by rank weight!
                    val rankIndex = 99 - (getPrimaryGroupOrNull()?.weight?.orElse(0) ?: 0)
                    "$rankIndex-$playerUUID"
                },
                { _, preferredLocale -> getTagColor(preferredLocale) },
                { _, preferredLocale -> getRankPrefix(preferredLocale) },
                suffixSupplier,
                belowNameSupplier
            )
        }

        return finalPlayer
    }

    /** @returns the primary LuckPerms group for this player. */
    public fun getPrimaryGroupName(): String {
        val api = LuckPermsProvider.get()
        val user = api.getPlayerAdapter(Player::class.java).getUser(onlinePlayer)

        return user.primaryGroup
    }

    /** @returns the primary LuckPerms group for this player. */
    public fun getPrimaryGroupOrNull(): Group? {
        val api = LuckPermsProvider.get()

        return api.groupManager.getGroup(getPrimaryGroupName())
    }

    /** @returns the decorated rank name of this player. */
    public fun getDecoratedRankName(locale: Locale = onlinePlayer.locale()): Component {
        val globalTranslations = ThankmasPlugin.instance().globalTranslations
        val group = getPrimaryGroupName()

        return globalTranslations.translate("rank.$group.name", locale)
            .color(
                globalTranslations.translate("rank.$group.color", locale).color()
            )
    }

    /** @returns the tag color for this player's rank. */
    public fun getTagColor(locale: Locale? = null): NamedTextColor = NamedTextColor.nearestTo(
        globalTranslations.translate(
            "rank.${getPrimaryGroupName()}.color",
            locale ?: globalTranslations.defaultLocale
        ).color() ?: NamedTextColor.BLACK
    )

    /** @returns the prefix for this player's rank. */
    public fun getRankPrefix(locale: Locale? = null): Component = globalTranslations.translate(
        "rank.${getPrimaryGroupName()}.prefix",
        locale ?: globalTranslations.defaultLocale
    ).append(Component.space())

    /** @returns the full player name tag with their rank prefix. */
    public fun getNameTag(locale: Locale = onlinePlayer.locale()): Component {
        val finalPlayer = onlinePlayer

        return getRankPrefix(locale).append(Component.text(finalPlayer.name).color(getTagColor(locale)))
    }

}