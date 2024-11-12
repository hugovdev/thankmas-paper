package me.hugo.thankmas.items

import com.google.common.collect.LinkedHashMultimap
import dev.kezz.miniphrase.MiniPhrase
import dev.kezz.miniphrase.tag.TagResolverBuilder
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import me.hugo.thankmas.DefaultTranslations
import me.hugo.thankmas.config.enumOrNull
import me.hugo.thankmas.lang.TranslatedComponent
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import java.util.*

/** Item with translatable name and lore. */
public class TranslatableItem(
    private val material: Material = Material.PHANTOM_MEMBRANE,
    private val customModelData: Int = -1,
    private val model: String? = null,
    private val unbreakable: Boolean = false,
    private val flags: List<ItemFlag> = emptyList(),
    private val name: String? = null,
    private val lore: String? = null,
    private val glint: Boolean? = null,
    private val enchantments: Map<Enchantment, Int> = emptyMap(),
    private val color: Int = -1,
    override val miniPhrase: MiniPhrase = DefaultTranslations.instance.translations
) : TranslatedComponent {

    public val nameNotNull: String
        get() = requireNotNull(name) { "Tried to get a null translatable name!" }

    public val loreNotNull: String
        get() = requireNotNull(lore) { "Tried to get a null translatable lore!" }

    /** Reading every value from a config file! */
    public constructor(
        config: FileConfiguration,
        path: String,
        miniPhrase: MiniPhrase = DefaultTranslations.instance.translations
    ) : this(
        config.enumOrNull<Material>("$path.material") ?: Material.PHANTOM_MEMBRANE,
        config.getInt("$path.custom-model-data", -1),
        config.getString("$path.model"),
        config.getBoolean("$path.unbreakable", false),
        config.getStringList("$path.flags").map { ItemFlag.valueOf(it.uppercase()) }.toList(),
        config.getString("$path.name"),
        config.getString("$path.lore"),
        // Only replace enchantment glint when explicitly specified.
        if (config.contains("$path.enchant-glint")) config.getBoolean("$path.enchant-glint") else null,
        config.getStringList("enchantments").associate {
            val serializedParts = it.split(", ")

            require(serializedParts.size == 2)
            { "Tried to apply enchantment to item in $path but it doesn't follow the correct config format. (enchantment_name, level)" }

            val enchantmentName = serializedParts[0]
            val enchantmentKey = requireNotNull(NamespacedKey.fromString(enchantmentName))
            { "Could not find enchantment with name $enchantmentName." }

            Pair(
                RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).getOrThrow(enchantmentKey),
                serializedParts[1].toInt()
            )
        },
        config.getInt("$path.color", -1),
        miniPhrase
    )

    // Build the base item with every shared attribute!
    private val baseItem = ItemStack(material)
        .apply {
            if (ItemFlag.HIDE_ATTRIBUTES in flags) setAttributeModifiers(LinkedHashMultimap.create())

            // Assign all special ItemMeta!
            editMeta {
                // Enchant glint overrides!
                if (glint != null) it.setEnchantmentGlintOverride(glint)

                // Item model overrides!
                if (model != null) it.itemModel = NamespacedKey.fromString(model)
                if (customModelData != -1) it.setCustomModelData(customModelData)

                // Item Flags!
                if (flags.isNotEmpty()) it.addItemFlags(*flags.toTypedArray())

                it.isUnbreakable = unbreakable
            }

            // Tint leather armor!
            run tint@{
                if (color == -1) return@tint

                color(Color.fromRGB(color))
            }

            this@TranslatableItem.enchantments.forEach { (enchantment, level) ->
                addEnchantment(enchantment, level)
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
        ItemStack(baseItem).nameTranslatable(nameNotNull, locale, tags)
            .loreTranslatable(loreNotNull, locale, tags)

    /** @returns a copy of the base item. */
    public fun getBaseItem(): ItemStack = ItemStack(baseItem)
}