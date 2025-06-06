package me.hugo.thankmas.items

import com.google.common.collect.Multimap
import dev.kezz.miniphrase.MiniPhraseContext
import dev.kezz.miniphrase.tag.TagResolverBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.block.banner.Pattern
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BannerMeta
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.persistence.PersistentDataType
import java.util.*

/** Style reset for lore and names. (Removes italics and ugly lore color) */
private val resetStyles = Style.style(NamedTextColor.WHITE, TextDecoration.ITALIC.withState(false))

/** Changes the amount of this item stack to [amount]. */
public fun ItemStack.amount(amount: Int): ItemStack {
    setAmount(amount)
    return this
}

/**
 * Sets the name to the translation [key] in
 * language [locale] and uses TagResolvers in [tags].
 */
context(context: MiniPhraseContext)
public fun ItemStack.nameTranslatable(
    key: String,
    locale: Locale,
    tags: (TagResolverBuilder.() -> Unit)? = null
): ItemStack {
    val meta = itemMeta
    meta.displayName(context.miniPhrase.translate(key, locale, tags).applyFallbackStyle(resetStyles))
    itemMeta = meta
    return this
}

/**
 * Sets the lore to the translation [key] in
 * language [locale] and uses TagResolvers in [tags].
 */
context(context: MiniPhraseContext)
public fun ItemStack.loreTranslatable(
    key: String,
    locale: Locale,
    tags: (TagResolverBuilder.() -> Unit)? = null
): ItemStack {
    lore(context.miniPhrase.translateList(key, locale, tags).map { it.applyFallbackStyle(resetStyles) })
    return this
}

/**
 * Adds the translation [key] in language [locale]
 * to the current lore and uses TagResolvers in [tags].
 */
context(context: MiniPhraseContext)
public fun ItemStack.addLoreTranslatable(
    key: String,
    locale: Locale,
    tags: (TagResolverBuilder.() -> Unit)? = null
): ItemStack {
    lore(
        (lore() ?: mutableListOf()).plus(
            context.miniPhrase.translateList(key, locale, tags).map { it.applyFallbackStyle(resetStyles) })
    )
    return this
}

/**
 * Adds the translation [key] in language [locale]
 * to the current lore and uses TagResolvers in [tags].
 */
context(context: MiniPhraseContext)
public fun ItemStack.addLoreTranslatableIf(
    key: String,
    locale: Locale,
    tags: (TagResolverBuilder.() -> Unit)? = null,
    predicate: () -> Boolean,
): ItemStack {
    if (!predicate.invoke()) return this

    lore(
        (lore() ?: mutableListOf()).plus(
            context.miniPhrase.translateList(key, locale, tags).map { it.applyFallbackStyle(resetStyles) })
    )
    return this
}

/** Sets the custom model data of this item stack. */
public fun ItemStack.customModelData(id: Int): ItemStack {
    if (id == -1) return this

    val meta = itemMeta
    meta.setCustomModelData(id)
    itemMeta = meta
    return this
}

/** Sets the custom model data of this item stack. */
public fun ItemStack.model(model: String?): ItemStack {
    if (model == null) return this

    val meta = itemMeta
    meta.itemModel = NamespacedKey.fromString(model)
    itemMeta = meta
    return this
}

/** Changes whether this item is unbreakable. */
public fun ItemStack.unbreakable(unbreakable: Boolean): ItemStack {
    val meta = itemMeta
    meta.isUnbreakable = unbreakable
    itemMeta = meta
    return this
}

/** Adds an attribute modifier. */
public fun ItemStack.attributeModifier(attribute: Attribute, modifier: AttributeModifier): ItemStack {
    val meta = itemMeta
    meta.addAttributeModifier(attribute, modifier)
    itemMeta = meta
    return this
}

/** Adds an attribute modifier. */
public fun ItemStack.setAttributeModifiers(modifiers: Multimap<Attribute, AttributeModifier>?): ItemStack {
    val meta = itemMeta
    meta.attributeModifiers = modifiers
    itemMeta = meta
    return this
}

/** Removes an attribute modifier. */
public fun ItemStack.removeAttributeModifier(attribute: Attribute): ItemStack {
    val meta = itemMeta
    meta.removeAttributeModifier(attribute)
    itemMeta = meta
    return this
}

/** Saves [value] of type [dataType] in the key [key] in this item stack. */
public fun <T, V : Any> ItemStack.setKeyedData(key: String, dataType: PersistentDataType<T, V>, value: V): ItemStack {
    return setKeyedData(NamespacedKey("thankmas", key), dataType, value)
}

/** Saves [value] of type [dataType] in the key [key] in this item stack. */
public fun <T, V : Any> ItemStack.setKeyedData(
    key: NamespacedKey,
    dataType: PersistentDataType<T, V>,
    value: V
): ItemStack {
    val meta = itemMeta
    meta.persistentDataContainer.set(key, dataType, value)
    itemMeta = meta
    return this
}

/** @returns whether this item has data saved in [key] of type [type]. */
public fun <T : Any, V : Any> ItemStack.hasKeyedData(key: String, type: PersistentDataType<T, V>): Boolean =
    hasKeyedData(NamespacedKey("thankmas", key), type)

/** @returns whether this item has data saved in [key] of type [type]. */
public fun <T : Any, V : Any> ItemStack.hasKeyedData(key: NamespacedKey, type: PersistentDataType<T, V>): Boolean =
    if (hasItemMeta()) {
        itemMeta?.persistentDataContainer?.has(key, type) ?: false
    } else {
        false
    }

/** @returns whether this item has data saved in [key]. */
public fun ItemStack.hasKeyedData(key: NamespacedKey): Boolean =
    if (hasItemMeta()) {
        itemMeta?.persistentDataContainer?.has(key) ?: false
    } else {
        false
    }

/** @returns whether this item has data saved in [key]. */
public fun ItemStack.hasKeyedData(key: String): Boolean =
    hasKeyedData(NamespacedKey("thankmas", key))

/** @returns the data of type [type] in the key [key], can be null. */
public fun <T : Any, V : Any> ItemStack.getKeyedData(key: String, type: PersistentDataType<T, V>): V? =
    getKeyedData(NamespacedKey("thankmas", key), type)

/** @returns the data of type [type] in the key [key], can be null. */
public fun <T : Any, V : Any> ItemStack.getKeyedData(key: NamespacedKey, type: PersistentDataType<T, V>): V? =
    if (hasItemMeta()) {
        itemMeta?.persistentDataContainer?.get(key, type)
    } else {
        null
    }

/** Changes the display name of this item stack to [name]. */
public fun ItemStack.name(name: Component): ItemStack {
    val meta = itemMeta
    meta.displayName(name.applyFallbackStyle(resetStyles))
    itemMeta = meta
    return this
}

/** Changes this item's lore to [text]. */
public fun ItemStack.putLore(text: List<Component>): ItemStack {
    this.lore(text.map { it.applyFallbackStyle(resetStyles) })
    return this
}

/** Adds [text] to the existing lore. */
public fun ItemStack.addToLore(text: List<Component>): ItemStack {
    this.lore((lore() ?: listOf()).plus(text.map { it.applyFallbackStyle(resetStyles) }))
    return this
}

/** Adds [text] to the existing lore. */
public fun ItemStack.addToLore(text: Component): ItemStack {
    this.lore((lore() ?: listOf()).plus(text.applyFallbackStyle(resetStyles)))
    return this
}

/** Applies [patterns] to this Banner item stack. */
public fun ItemStack.putPatterns(vararg patterns: Pattern): ItemStack {
    val meta = itemMeta as? BannerMeta ?: return this
    patterns.forEach { meta.addPattern(it) }
    itemMeta = meta
    return this
}

/** Adds enchantment glint effect if [selected] is true. */
public fun ItemStack.selectedEffect(selected: Boolean): ItemStack {
    return if (selected) {
        enchantment(Enchantment.LUCK_OF_THE_SEA)
        flags(ItemFlag.HIDE_ENCHANTS)
    } else this
}

/** Applies [enchantment] of level [level] to this item stack. */
public fun ItemStack.enchantment(enchantment: Enchantment, level: Int): ItemStack {
    addUnsafeEnchantment(enchantment, level)
    return this
}

/** Applies [enchantment] of level 1 to this item stack. */
public fun ItemStack.enchantment(enchantment: Enchantment): ItemStack {
    addUnsafeEnchantment(enchantment, 1)
    return this
}

/** Clears the enchantments of this item stack. */
public fun ItemStack.clearEnchantments(): ItemStack {
    enchantments.keys.forEach { this.removeEnchantment(it) }
    return this
}

/** Changes the color of this leather armor piece to [color]. */
public fun ItemStack.color(color: Color): ItemStack {
    if (type in listOf(
            Material.LEATHER_BOOTS,
            Material.LEATHER_CHESTPLATE,
            Material.LEATHER_HELMET,
            Material.LEATHER_LEGGINGS,
            Material.LEATHER_HORSE_ARMOR
        )
    ) {
        editMeta(LeatherArmorMeta::class.java) { it.setColor(color) }
        return this
    } else {
        throw IllegalArgumentException("Colors only applicable for leather armor!")
    }
}

/**  Adds [flags] to this item. */
public fun ItemStack.flags(vararg flags: ItemFlag): ItemStack {
    val meta = itemMeta
    meta.addItemFlags(*flags)
    itemMeta = meta
    return this
}