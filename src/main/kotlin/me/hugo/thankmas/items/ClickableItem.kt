package me.hugo.thankmas.items

import me.hugo.thankmas.lang.TranslatedComponent
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

/**  Item that can be clicked to run a command. */
public class ClickableItem(public val id: String, config: FileConfiguration, configPath: String) :
    TranslatedComponent {

    public companion object {
        public val CLICKABLE_ITEM_ID: NamespacedKey = NamespacedKey("thankmas", "clickable_item_id")
    }

    // lang -> item
    private val items: MutableMap<Locale, ItemStack> = mutableMapOf()
    private val slot: Int

    public val command: String

    init {
        val material = Material.valueOf(config.getString("$configPath.material") ?: "BEDROCK")
        val nameTranslation = config.getString("$configPath.name") ?: "$configPath.name"
        val loreTranslation = config.getString("$configPath.lore") ?: "$configPath.lore"

        command = config.getString("$configPath.command") ?: "help"
        slot = config.getInt("$configPath.slot")

        miniPhrase.translationRegistry.getLocales().forEach {
            val item = ItemStack(material)
                .nameTranslatable(nameTranslation, it)
                .loreTranslatable(loreTranslation, it)
                .flags(
                    ItemFlag.HIDE_ATTRIBUTES,
                    ItemFlag.HIDE_ITEM_SPECIFICS,
                    ItemFlag.HIDE_ENCHANTS,
                    ItemFlag.HIDE_DYE,
                    ItemFlag.HIDE_ARMOR_TRIM
                )
                .setKeyedData(CLICKABLE_ITEM_ID, PersistentDataType.STRING, id)

            items[it] = item
        }
    }

    /** Give to [player] this item. */
    public fun give(player: Player) {
        val locale = player.locale()

        val item = items[locale] ?: return
        player.inventory.setItem(slot, item)
    }

}