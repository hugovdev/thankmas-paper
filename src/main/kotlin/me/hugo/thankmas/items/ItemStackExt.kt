package me.hugo.thankmas.items

import dev.kezz.miniphrase.MiniPhraseContext
import dev.kezz.miniphrase.tag.TagResolverBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.NamespacedKey
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
context(MiniPhraseContext)
public fun ItemStack.nameTranslatable(
    key: String,
    locale: Locale,
    tags: (TagResolverBuilder.() -> Unit)? = null
): ItemStack {
    val meta = itemMeta
    meta.displayName(miniPhrase.translate(key, locale, tags).applyFallbackStyle(resetStyles))
    itemMeta = meta
    return this
}

/**
 * Sets the lore to the translation [key] in
 * language [locale] and uses TagResolvers in [tags].
 */
context(MiniPhraseContext)
public fun ItemStack.loreTranslatable(
    key: String,
    locale: Locale,
    tags: (TagResolverBuilder.() -> Unit)? = null
): ItemStack {
    lore(miniPhrase.translateList(key, locale, tags).map { it.applyFallbackStyle(resetStyles) })
    return this
}

/**
 * Adds the translation [key] in language [locale]
 * to the current lore and uses TagResolvers in [tags].
 */
context(MiniPhraseContext)
public fun ItemStack.addLoreTranslatable(
    key: String,
    locale: Locale,
    tags: (TagResolverBuilder.() -> Unit)? = null
): ItemStack {
    lore(
        (lore() ?: mutableListOf()).plus(
            miniPhrase.translateList(key, locale, tags).map { it.applyFallbackStyle(resetStyles) })
    )
    return this
}

/**
 * Adds the translation [key] in language [locale]
 * to the current lore and uses TagResolvers in [tags].
 */
context(MiniPhraseContext)
public fun ItemStack.addLoreTranslatableIf(
    key: String,
    locale: Locale,
    tags: (TagResolverBuilder.() -> Unit)? = null,
    predicate: () -> Boolean,
): ItemStack {
    if (!predicate.invoke()) return this

    lore(
        (lore() ?: mutableListOf()).plus(
            miniPhrase.translateList(key, locale, tags).map { it.applyFallbackStyle(resetStyles) })
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

/** Changes whether this item is unbreakable. */
public fun ItemStack.unbreakable(unbreakable: Boolean): ItemStack {
    val meta = itemMeta
    meta.isUnbreakable = unbreakable
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
public fun <T, V : Any> ItemStack.hasKeyedData(key: String, type: PersistentDataType<T, V>): Boolean =
    hasKeyedData(NamespacedKey("thankmas", key), type)

/** @returns whether this item has data saved in [key] of type [type]. */
public fun <T, V : Any> ItemStack.hasKeyedData(key: NamespacedKey, type: PersistentDataType<T, V>): Boolean =
    if (hasItemMeta()) {
        itemMeta?.persistentDataContainer?.has(key, type) ?: false
    } else {
        false
    }

/** @returns the data of type [type] in the key [key], can be null. */
public fun <T, V : Any> ItemStack.getKeyedData(key: String, type: PersistentDataType<T, V>): V? =
    getKeyedData(NamespacedKey("thankmas", key), type)

/** @returns the data of type [type] in the key [key], can be null. */
public fun <T, V : Any> ItemStack.getKeyedData(key: NamespacedKey, type: PersistentDataType<T, V>): V? =
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
    if (type == Material.LEATHER_BOOTS
        || type == Material.LEATHER_CHESTPLATE
        || type == Material.LEATHER_HELMET
        || type == Material.LEATHER_LEGGINGS
    ) {

        val meta = itemMeta as LeatherArmorMeta
        meta.setColor(color)
        itemMeta = meta
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