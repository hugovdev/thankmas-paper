package me.hugo.thankmas.items

import dev.kezz.miniphrase.MiniPhrase
import dev.kezz.miniphrase.tag.TagResolverBuilder
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.*
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import me.hugo.thankmas.DefaultTranslations
import me.hugo.thankmas.config.enumOrNull
import me.hugo.thankmas.lang.TranslatedComponent
import net.kyori.adventure.key.Key
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

/** Item with translatable name and lore. */
public class TranslatableItem(
    private val material: Material = Material.PHANTOM_MEMBRANE,
    public val amount: Int = 1,
    private val customModelData: Int = -1,
    private val model: String? = null,
    private val unbreakable: Boolean = false,
    private val flags: List<ItemFlag> = emptyList(),
    private val tags: List<String> = emptyList(),
    private val name: String? = null,
    private val lore: String? = null,
    private val glint: Boolean? = null,
    private val cooldown: Pair<Float, String?> = Pair(0.0f, null),
    private val equipabbleSlot: EquipmentSlot? = null,
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
        config.getInt("$path.amount", 1),
        config.getInt("$path.custom-model-data", -1),
        config.getString("$path.model"),
        config.getBoolean("$path.unbreakable", false),
        config.getStringList("$path.flags").map { ItemFlag.valueOf(it.uppercase()) }.toList(),
        config.getStringList("$path.tags"),
        config.getString("$path.name"),
        config.getString("$path.lore"),
        // Only replace enchantment glint when explicitly specified.
        if (config.contains("$path.enchant-glint")) config.getBoolean("$path.enchant-glint") else null,
        Pair(config.getDouble("$path.cooldown.time").toFloat(), config.getString("$path.cooldown.group")),
        config.enumOrNull<EquipmentSlot>("$path.equippable-slot"),
        config.getStringList("$path.enchantments").associate {
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
    private val baseItem = ItemStack(material, amount).apply {
        unbreakable(unbreakable)

        if (flags.isNotEmpty()) addItemFlags(*flags.toTypedArray())

        if (glint != null) {
            setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, glint)
        }

        if (model != null) {
            setData(DataComponentTypes.ITEM_MODEL, Key.key(model))
        }

        // Cooldown component
        if (cooldown.first > 0.0) {
            setData(
                DataComponentTypes.USE_COOLDOWN, UseCooldown.useCooldown(cooldown.first)
                    .cooldownGroup(NamespacedKey("thankmas", requireNotNull(cooldown.second)))
            )
        }

        // Makes this item equippable for the selected slot.
        if (equipabbleSlot != null) {
            setData(DataComponentTypes.EQUIPPABLE, Equippable.equippable(equipabbleSlot).build())
        }

        // Tint leather armor!
        run tint@{
            if (color == -1) return@tint

            setData(DataComponentTypes.DYED_COLOR, DyedItemColor.dyedItemColor(Color.fromRGB(color)))
        }

        this@TranslatableItem.enchantments.forEach { (enchantment, level) ->
            addEnchantment(enchantment, level)
        }

        tags.forEach { setKeyedData(it.lowercase(), PersistentDataType.BOOLEAN, true) }

        if (ItemFlag.HIDE_ATTRIBUTES in flags) {
            setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hiddenComponents(dataTypes).build())
        }
    }

    /** Lets other classes edit details from this translatable item. */
    public fun editBaseItem(editor: (item: ItemStack) -> Unit) {
        editor(baseItem)
    }

    /** @returns a supplier to build this item for [viewer]. */
    public fun supplier(viewer: Player, tags: TagResolverBuilder.() -> Unit): (player: Player) -> ItemStack =
        { buildItem(viewer.locale(), tags) }

    /** Builds this item in [locale]. */
    public fun buildItem(locale: Locale, tags: (TagResolverBuilder.() -> Unit)? = null): ItemStack =
        ItemStack(baseItem).apply {
            val nameKey = this@TranslatableItem.name
            if (nameKey != null) nameTranslatable(nameKey, locale, tags)

            val loreKey = this@TranslatableItem.lore
            if (loreKey != null) loreTranslatable(loreKey, locale, tags)
        }

    /** Builds this item for [player]. */
    public fun buildItem(player: Player, tags: (TagResolverBuilder.() -> Unit)? = null): ItemStack =
        buildItem(player.locale(), tags)

    /** @returns a copy of the base item. */
    public fun getBaseItem(): ItemStack = ItemStack(baseItem)
}