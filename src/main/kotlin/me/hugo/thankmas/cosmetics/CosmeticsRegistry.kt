package me.hugo.thankmas.cosmetics

import me.hugo.thankmas.SimpleThankmasPlugin
import me.hugo.thankmas.ThankmasPlugin
import me.hugo.thankmas.config.ConfigurationProvider
import me.hugo.thankmas.gui.Icon
import me.hugo.thankmas.gui.Menu
import me.hugo.thankmas.gui.PaginatedMenu
import me.hugo.thankmas.items.name
import me.hugo.thankmas.items.putLore
import me.hugo.thankmas.player.cosmetics.CosmeticsPlayerData
import me.hugo.thankmas.player.playSound
import me.hugo.thankmas.player.stopSound
import me.hugo.thankmas.player.translate
import me.hugo.thankmas.registry.AutoCompletableMapRegistry
import net.kyori.adventure.sound.Sound
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Registry of player cosmetics! */
@Single
public class CosmeticsRegistry : AutoCompletableMapRegistry<Cosmetic>(Cosmetic::class.java), KoinComponent {

    private val globalTranslations = SimpleThankmasPlugin.instance().globalTranslations

    /** Cosmetics selector menu. */
    private val cosmeticsSelector: PaginatedMenu = PaginatedMenu(
        "menu.cosmetics_selector.title.on", 9 * 6,
        Menu.MenuFormat.FOUR_SLIM_ROWS, null, null,
        globalTranslations
    )

    init {
        val configurationProvider: ConfigurationProvider by inject()
        val config = configurationProvider.getOrLoad("global/cosmetics.yml")

        cosmeticsSelector.addIcon(Icon({ context, _ ->
            val clicker = context.clicker

            val playerData =
                ThankmasPlugin.instance().playerDataManager.getPlayerData(clicker.uniqueId) as CosmeticsPlayerData

            if (playerData.selectedCosmetic.value == null) return@Icon

            playerData.selectedCosmetic.value = null

            clicker.stopSound("lobby.cosmetic_selector_open", Sound.Source.AMBIENT)
            clicker.closeInventory()
        }) {
            ItemStack(Material.BARRIER)
                .name(globalTranslations.translate("cosmetics.remove.name", it.locale()))
                .putLore(globalTranslations.translateList("cosmetics.remove.lore", it.locale()))
        })

        config.getKeys(false).forEach { cosmeticId ->
            Cosmetic(config, cosmeticId).also { cosmetic ->
                register(cosmeticId, cosmetic)

                cosmeticsSelector.addIcon(Icon({ context, _ ->
                    val clicker = context.clicker

                    val playerData =
                        ThankmasPlugin.instance().playerDataManager.getPlayerData(clicker.uniqueId) as CosmeticsPlayerData

                    if (playerData.selectedCosmetic.value == cosmetic) return@Icon

                    // Player owns this cosmetic, so just equip it!
                    if (playerData.ownsCosmetic(cosmetic)) {
                        clicker.sendMessage(
                            globalTranslations.translate(
                                "cosmetics.cosmetic.equip",
                                clicker.locale()
                            ) {
                                inserting("cosmetic", clicker.translate(cosmetic.nameKey).color(null))
                            })

                        playerData.selectedCosmetic.value = cosmetic
                        clicker.playSound("lobby.cosmetic_selector_select")
                    } else {
                        if (cosmetic.price == 0) {
                            clicker.sendMessage(
                                globalTranslations.translate(
                                    "cosmetics.cosmetic.quest.${cosmetic.id}",
                                    clicker.locale()
                                )
                            )

                            clicker.stopSound("lobby.cosmetic_selector_open", Sound.Source.AMBIENT)
                            clicker.closeInventory()
                            return@Icon
                        }

                        if (!context.clickType.isShiftClick) return@Icon
                        if (playerData.inTransaction) return@Icon

                        if (playerData.currency >= cosmetic.price) {
                            playerData.acquireCosmetic(cosmetic) {
                                clicker.sendMessage(
                                    globalTranslations.translate(
                                        "cosmetics.cosmetic.bought",
                                        clicker.locale()
                                    ) {
                                        inserting("cosmetic", clicker.translate(cosmetic.nameKey).color(null))
                                    })

                                playerData.selectedCosmetic.value = cosmetic
                                clicker.playSound("lobby.cosmetic_selector_buy")
                            }
                        } else {
                            clicker.sendMessage(
                                globalTranslations.translate(
                                    "cosmetics.cosmetic.broke",
                                    clicker.locale()
                                ) {
                                    inserting("cosmetic", clicker.translate(cosmetic.nameKey).color(null))
                                })
                        }
                    }

                    clicker.stopSound("lobby.cosmetic_selector_open", Sound.Source.AMBIENT)
                    clicker.closeInventory()

                    // Remove ghost items from shift clicking
                    clicker.updateInventory()
                }) { player ->
                    val slotKey = cosmetic.slot.name.lowercase()

                    val playerData =
                        ThankmasPlugin.instance().playerDataManager.getPlayerData(player.uniqueId) as CosmeticsPlayerData

                    val selected = playerData.selectedCosmetic.value == cosmetic

                    cosmetic.item.buildItem(player)
                        .putLore(
                            globalTranslations.translateList(
                                if (selected) "cosmetics.$slotKey.selected"
                                else if (playerData.ownsCosmetic(cosmetic)) "cosmetics.$slotKey.selectable"
                                else if (cosmetic.price > 0) "cosmetics.$slotKey.priced"
                                else "cosmetics.$slotKey.exclusive",
                                player.locale()
                            ) {
                                parsed("price", cosmetic.price)
                            }
                        ).also { if (selected) it.editMeta { it.setEnchantmentGlintOverride(true) } }
                }.listen { (ThankmasPlugin.instance().playerDataManager.getPlayerData(it.uniqueId) as CosmeticsPlayerData).selectedCosmetic })
            }
        }
    }

    /** Opens the cosmetics selector for [player]. */
    public fun openSelector(player: Player): Unit = cosmeticsSelector.open(player).also {
        player.playSound("lobby.cosmetic_selector_open")
    }
}