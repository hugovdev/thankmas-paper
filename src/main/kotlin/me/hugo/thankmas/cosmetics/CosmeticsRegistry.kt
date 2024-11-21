package me.hugo.thankmas.cosmetics

import dev.kezz.miniphrase.audience.sendTranslated
import me.hugo.thankmas.ThankmasPlugin
import me.hugo.thankmas.gui.Icon
import me.hugo.thankmas.gui.Menu
import me.hugo.thankmas.gui.paginated.PaginatedMenu
import me.hugo.thankmas.items.loreTranslatable
import me.hugo.thankmas.lang.Translated
import me.hugo.thankmas.player.cosmetics.CosmeticsPlayerData
import me.hugo.thankmas.player.translate
import me.hugo.thankmas.registry.AutoCompletableMapRegistry
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.koin.core.annotation.Single

/** Registry of player cosmetics! */
@Single
public class CosmeticsRegistry(config: FileConfiguration) :
    AutoCompletableMapRegistry<Cosmetic>(Cosmetic::class.java),
    Translated {

    /** Cosmetics selector menu. */
    private val cosmeticsSelector: PaginatedMenu = PaginatedMenu(
        "menu.cosmetics_selector.title", 9 * 6,
        Menu.MenuFormat.FOUR_SLIM_ROWS, null, null
    )

    init {
        config.getKeys(false).forEach { cosmeticId ->
            Cosmetic(config, cosmeticId).also { cosmetic ->
                register(cosmeticId, cosmetic)

                cosmeticsSelector.addIcon(Icon({ context, _ ->
                    val clicker = context.clicker

                    val playerData =
                        ThankmasPlugin.instance().playerDataManager.getPlayerData(clicker.uniqueId) as CosmeticsPlayerData

                    if (playerData.selectedCosmetic.value == cosmetic) return@Icon

                    clicker.sendTranslated("cosmetics.cosmetic.equip") {
                        inserting("cosmetic", clicker.translate(cosmetic.nameKey).color(null))
                    }

                    playerData.selectedCosmetic.value = cosmetic
                }) { player ->
                    val slotKey = cosmetic.slot.name.lowercase()

                    val playerData =
                        ThankmasPlugin.instance().playerDataManager.getPlayerData(player.uniqueId) as CosmeticsPlayerData

                    val selected = playerData.selectedCosmetic.value == cosmetic

                    cosmetic.item.buildItem(player)
                        .loreTranslatable(
                            if (selected) "cosmetics.$slotKey.selected"
                            else if(playerData.ownsCosmetic(cosmetic)) "cosmetics.$slotKey.selectable"
                            else if (cosmetic.price > 0) "cosmetics.$slotKey.priced"
                            else "cosmetics.$slotKey.exclusive",
                            player.locale()
                        ) {
                            parsed("price", cosmetic.price)
                        }.also { if (selected) it.editMeta { it.setEnchantmentGlintOverride(true) } }
                }.listen { (ThankmasPlugin.instance().playerDataManager.getPlayerData(it.uniqueId) as CosmeticsPlayerData).selectedCosmetic })
            }
        }
    }

    /** Opens the cosmetics selector for [player]. */
    public fun openSelector(player: Player): Unit = cosmeticsSelector.open(player).also {
        player.playSound(Sound.sound(Key.key("lobby.cosmetic_selector_open"), Sound.Source.AMBIENT, 1.0f, 1.0f))
    }
}