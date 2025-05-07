package me.hugo.thankmas.player.cosmetics

import me.hugo.thankmas.ThankmasPlugin
import me.hugo.thankmas.cosmetics.Cosmetic
import me.hugo.thankmas.cosmetics.CosmeticsRegistry
import me.hugo.thankmas.player.PlayerBasics
import me.hugo.thankmas.player.rank.RankedPlayerData
import me.hugo.thankmas.player.updateBoardTags
import me.hugo.thankmas.state.StatefulValue
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.component.inject
import java.util.*

/** Special PlayerData implementation that load up cosmetics and offers hooks to fetch and save data. */
public open class CosmeticsPlayerData<P : CosmeticsPlayerData<P>>(
    playerUUID: UUID, instance: ThankmasPlugin<P>,
    public val doesUpdateCosmetic: (player: Player) -> Boolean = { _ -> true }
) : RankedPlayerData<P>(playerUUID, instance.playerDataManager) {

    private val cosmeticRegistry: CosmeticsRegistry by inject()

    public var inTransaction: Boolean = false
        protected set

    /** List of cosmetics this player owns. */
    protected lateinit var wardrobe: PlayerWardrobe
        private set

    /** List of cosmetics this player owns. */
    protected lateinit var basics: PlayerBasics
        private set

    /** The cosmetic this player is using. */
    public lateinit var selectedCosmetic: StatefulValue<Cosmetic?>
        private set

    public var currency: Int = 0
        set(value) {
            if (field == value) return

            field = value
            basics.currency = field

            updateBoardTags("currency")
        }

    override fun setLocale(newLocale: Locale) {
        super.setLocale(newLocale)

        giveCosmetic()
    }

    /** Acquires [cosmetic] for this player. */
    public fun acquireCosmetic(cosmetic: Cosmetic, onAcquired: () -> Unit = {}) {
        require(cosmetic !in wardrobe)
        require(currency >= cosmetic.price)

        val hasPrice = cosmetic.price > 0
        if (hasPrice) require(!inTransaction)

        val instance = ThankmasPlugin.instance<ThankmasPlugin<*>>()

        if (hasPrice) inTransaction = true

        Bukkit.getScheduler().runTaskAsynchronously(instance, Runnable {
            wardrobe.cosmetics += UnlockedCosmetic(cosmetic.id)

            transaction {
                playerPropertyManager.getProperty<PlayerWardrobe>().write(playerUUID, wardrobe)
            }

            Bukkit.getScheduler().runTask(instance, Runnable {
                if (hasPrice) currency -= cosmetic.price

                onAcquired()

                if (hasPrice) inTransaction = false
            })
        })
    }

    /** @returns whether this player owns [cosmetic]. */
    public fun ownsCosmetic(cosmetic: Cosmetic): Boolean = cosmetic in wardrobe

    /** Gives this player their selected cosmetic. */
    public fun giveCosmetic() {
        val cosmetic = selectedCosmetic.value ?: return

        val bukkitPlayer = onlinePlayer

        if (!doesUpdateCosmetic(bukkitPlayer)) return

        bukkitPlayer.inventory.setItem(cosmetic.slot, cosmetic.item.buildItem(bukkitPlayer))
    }

    override fun onLoading() {
        super.onLoading()

        // Load all basics: currencies, selected rods and cosmetics.
        basics = playerPropertyManager.getProperty<PlayerBasics>().get(playerUUID)
        currency = basics.currency

        // Load all the cosmetics this player owns.
        wardrobe = playerPropertyManager.getProperty<PlayerWardrobe>().get(playerUUID)

        val selectedCosmetic = cosmeticRegistry.getOrNull(basics.selectedCosmetic)

        this.selectedCosmetic = StatefulValue(selectedCosmetic.takeIf { it != null && ownsCosmetic(it) }).apply {
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