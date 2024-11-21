package me.hugo.thankmas.cosmetics

import me.hugo.thankmas.gui.Icon
import me.hugo.thankmas.gui.Menu
import me.hugo.thankmas.gui.paginated.PaginatedMenu
import me.hugo.thankmas.items.loreTranslatable
import me.hugo.thankmas.lang.Translated
import me.hugo.thankmas.registry.AutoCompletableMapRegistry
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.koin.core.annotation.Single

/** Registry of player cosmetics! */
@Single
public class CosmeticsRegistry(config: FileConfiguration) : AutoCompletableMapRegistry<Cosmetic>(Cosmetic::class.java),
    Translated {

    /** Cosmetics selector menu. */
    private val cosmeticsSelector: PaginatedMenu = PaginatedMenu(
        "menu.cosmetics_selector.title", 9 * 6,
        Menu.MenuFormat.FOUR_SLIM_ROWS, null, null
    )

    init {
        config.getKeys(false).forEach { cosmeticId ->
            Cosmetic(config, cosmeticId).also {
                register(cosmeticId, it)

                cosmeticsSelector.addIcon(Icon({ context, _ ->
                    val clicker = context.clicker
                    clicker.inventory.setItem(it.slot, it.item.buildItem(clicker))
                }) { player ->
                    val slotKey = it.slot.name.lowercase()

                    it.item.buildItem(player)
                        .loreTranslatable(
                            if (it.price > 0) "cosmetics.$slotKey.priced"
                            else "cosmetics.$slotKey.exclusive",
                            player.locale()
                        ) {
                            parsed("price", it.price)
                        }
                })
            }
        }
    }

    /** Opens the cosmetics selector for [player]. */
    public fun openSelector(player: Player): Unit = cosmeticsSelector.open(player)
}