package me.hugo.thankmas.player.cosmetics

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import me.hugo.thankmas.cosmetics.Cosmetic

/** Collection of every cosmetic unlocked by a player. */
@Serializable
public data class PlayerWardrobe(
    public val cosmetics: MutableList<UnlockedCosmetic> = mutableListOf()
) {
    public operator fun contains(cosmetic: Cosmetic): Boolean = cosmetic.id in cosmetics.map { it.cosmeticId }
    public operator fun contains(cosmeticId: String): Boolean = cosmeticId in cosmetics.map { it.cosmeticId }
}

/** Instance of an unlocked cosmetic. */
@Serializable
public data class UnlockedCosmetic(
    /** Id of the unlocked cosmetic. */
    public val cosmeticId: String,
    /** Time at which this cosmetic was unlocked. */
    public val unlockTime: Instant = Clock.System.now()
)