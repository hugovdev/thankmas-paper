package me.hugo.thankmas.items

import dev.kezz.miniphrase.MiniPhrase
import dev.kezz.miniphrase.MiniPhraseContext
import dev.kezz.miniphrase.tag.TagResolverBuilder
import me.hugo.thankmas.DefaultTranslations
import me.hugo.thankmas.config.string
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import java.util.*

/**
 * Item with translatable name and lore.
 */
public class TranslatableItem(
    config: FileConfiguration,
    path: String,
    override val miniPhrase: MiniPhrase = DefaultTranslations.instance.translations
) : MiniPhraseContext {

    private val material = Material.valueOf(config.string("$path.material"))
    private val customModelData = config.getInt("$path.custom-model-data")

    private val baseItem = ItemStack(material)
        .customModelData(customModelData)
        .unbreakable(config.getBoolean("$path.unbreakable", false))
        .flags(*config.getStringList("$path.flags").map { ItemFlag.valueOf(it.uppercase()) }.toTypedArray())

    public val name: String = config.string("$path.name")
    public val lore: String = config.string("$path.lore")

    init {
        config.getStringList("enchantments").forEach {
            val serializedParts = it.split(", ")
            requireNotNull(serializedParts.size == 2) { "Tried to apply enchantment to item in $path but it doesn't follow the correct config format. (enchantment_name, level)" }

            val enchantmentName = serializedParts[0]
            val enchantment = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(enchantmentName))
            requireNotNull(enchantment) { "Could not find enchantment with name $enchantmentName." }

            baseItem.addEnchantment(enchantment, serializedParts[1].toInt())
        }
    }

    /** Lets other classes edit details from this translatable item. */
    public fun editBaseItem(editor: (item: ItemStack) -> ItemStack) {
        editor(baseItem)
    }

    /** @returns a supplier to build this item for [viewer]. */
    public fun supplier(viewer: Player, tags: TagResolverBuilder.() -> Unit): (player: Player) -> ItemStack =
        { buildItem(viewer.locale(), tags) }

    /** Builds this item in [locale]. */
    public fun buildItem(locale: Locale, tags: (TagResolverBuilder.() -> Unit)? = null): ItemStack =
        ItemStack(baseItem).nameTranslatable(name, locale, tags).loreTranslatable(lore, locale, tags)

    /** @returns a copy of the base item. */
    public fun getBaseItem(): ItemStack = ItemStack(baseItem)
}