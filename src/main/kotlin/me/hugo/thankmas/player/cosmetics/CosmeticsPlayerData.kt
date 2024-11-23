package me.hugo.thankmas.player.cosmetics

import me.hugo.thankmas.ThankmasPlugin
import me.hugo.thankmas.cosmetics.Cosmetic
import me.hugo.thankmas.cosmetics.CosmeticsRegistry
import me.hugo.thankmas.database.CosmeticsOwned
import me.hugo.thankmas.database.PlayerData
import me.hugo.thankmas.player.rank.RankedPlayerData
import me.hugo.thankmas.state.StatefulValue
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll
import org.koin.core.component.inject
import java.util.*

/** Special PlayerData implementation that load up cosmetics and offers hooks to fetch and save data. */
public open class CosmeticsPlayerData<P : CosmeticsPlayerData<P>>(
    playerUUID: UUID, instance: ThankmasPlugin<P>,
    public val doesUpdateCosmetic: (player: Player) -> Boolean = { _ -> true }
) : RankedPlayerData<P>(playerUUID, instance.playerDataManager) {

    private val cosmeticRegistry: CosmeticsRegistry by inject()

    /** List of cosmetics this player owns. */
    private val cosmeticsOwned: MutableList<Cosmetic> = mutableListOf()

    /** The cosmetic this player is using. */
    public lateinit var selectedCosmetic: StatefulValue<Cosmetic?>
        private set

    public var currency: Int = 0
        private set

    override fun setLocale(newLocale: Locale) {
        super.setLocale(newLocale)

        giveCosmetic()
    }

    /** @returns whether this player owns [cosmetic]. */
    public fun ownsCosmetic(cosmetic: Cosmetic): Boolean = cosmeticsOwned.contains(cosmetic)

    /** Gives this player their selected cosmetic. */
    public fun giveCosmetic() {
        val cosmetic = selectedCosmetic.value ?: return

        val bukkitPlayer = onlinePlayer

        if (!doesUpdateCosmetic(bukkitPlayer)) return

        bukkitPlayer.inventory.setItem(cosmetic.slot, cosmetic.item.buildItem(bukkitPlayer))
    }

    protected fun loadCurrency(row: ResultRow?) {
        currency = row?.get(PlayerData.currency) ?: 0
    }

    protected fun loadCosmetics(row: ResultRow?) {
        // Load all the cosmetics this player has!
        CosmeticsOwned.selectAll().where { CosmeticsOwned.whoOwns eq playerUUID.toString() }.forEach { result ->
            val cosmeticId = result[CosmeticsOwned.cosmeticId]

            cosmeticsOwned += cosmeticRegistry.get(cosmeticId)
        }

        selectedCosmetic =
            StatefulValue(cosmeticRegistry.getOrNull(row?.get(PlayerData.selectedCosmetic) ?: "")).apply {
                subscribe { oldCosmetic, newCosmetic, _ ->
                    val bukkitPlayer = onlinePlayerOrNull ?: return@subscribe

                    if (!doesUpdateCosmetic(bukkitPlayer)) return@subscribe

                    // Clear the old cosmetic if the slot is a different one to the new one!
                    if (oldCosmetic != null && newCosmetic?.slot != oldCosmetic.slot) {
                        bukkitPlayer.inventory.setItem(oldCosmetic.slot, null)
                    }

                    if (newCosmetic == null) return@subscribe

                    bukkitPlayer.inventory.setItem(
                        newCosmetic.slot,
                        newCosmetic.item.buildItem(bukkitPlayer)
                    )
                }
            }
    }
}