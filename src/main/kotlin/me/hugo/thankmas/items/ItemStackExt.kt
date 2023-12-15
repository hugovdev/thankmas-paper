package me.hugo.thankmas.items

import dev.kezz.miniphrase.MiniPhraseContext
import dev.kezz.miniphrase.tag.TagResolverBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
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
import java.util.function.Consumer

public fun ItemStack.amount(amount: Int): ItemStack {
    setAmount(amount)
    return this
}

context(MiniPhraseContext)
public fun ItemStack.nameTranslatable(
    key: String,
    locale: Locale,
    tags: (TagResolverBuilder.() -> Unit)? = null
): ItemStack {
    val meta = itemMeta
    meta.displayName(
        Component.text("", NamedTextColor.WHITE)
            .decoration(TextDecoration.ITALIC, false)
            .append(miniPhrase.translate(key, locale, tags))
    )
    itemMeta = meta
    return this
}

public fun ItemStack.customModelData(id: Int): ItemStack {
    val meta = itemMeta
    meta.setCustomModelData(id)
    itemMeta = meta
    return this
}

context(MiniPhraseContext)
public fun ItemStack.loreTranslatable(
    key: String,
    locale: Locale,
    tags: (TagResolverBuilder.() -> Unit)? = null
): ItemStack {
    lore(miniPhrase.translateList(key, locale, tags).map {
        Component.text("", NamedTextColor.WHITE)
            .decoration(TextDecoration.ITALIC, false)
            .append(it)
    })
    return this
}

public fun <T, V : Any> ItemStack.setKeyedData(key: String, dataType: PersistentDataType<T, V>, value: V): ItemStack {
    return setKeyedData(NamespacedKey("stk", key), dataType, value)
}

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

public fun <T, V : Any> ItemStack.hasKeyedData(key: String, type: PersistentDataType<T, V>): Boolean =
    hasKeyedData(NamespacedKey("stk", key), type)

public fun <T, V : Any> ItemStack.hasKeyedData(key: NamespacedKey, type: PersistentDataType<T, V>): Boolean =
    if (hasItemMeta()) {
        itemMeta?.persistentDataContainer?.has(key, type) ?: false
    } else {
        false
    }

public fun <T, V : Any> ItemStack.getKeyedData(key: String, type: PersistentDataType<T, V>): V? =
    getKeyedData(NamespacedKey("stk", key), type)

public fun <T, V : Any> ItemStack.getKeyedData(key: NamespacedKey, type: PersistentDataType<T, V>): V? =
    if (hasItemMeta()) {
        itemMeta?.persistentDataContainer?.get(key, type)
    } else {
        null
    }

public fun ItemStack.name(name: Component): ItemStack {
    val meta = itemMeta
    meta.displayName(name)
    itemMeta = meta
    return this
}

public fun ItemStack.putLore(text: List<Component>): ItemStack {
    this.lore(text)
    return this
}

public fun ItemStack.putPatterns(vararg patterns: Pattern): ItemStack {
    val meta = itemMeta as BannerMeta
    patterns.forEach { meta.addPattern(it) }
    itemMeta = meta
    return this
}

public fun ItemStack.enchantment(enchantment: Enchantment?, level: Int): ItemStack {
    if (enchantment == null) return this
    addUnsafeEnchantment(enchantment, level)
    return this
}

public fun ItemStack.enchantment(enchantment: Enchantment): ItemStack {
    addUnsafeEnchantment(enchantment, 1)
    return this
}

public fun ItemStack.type(material: Material): ItemStack {
    type = material
    return this
}

public fun ItemStack.clearEnchantments(): ItemStack {
    enchantments.keys.forEach(Consumer { this.removeEnchantment(it) })
    return this
}

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

public fun ItemStack.flag(vararg flag: ItemFlag): ItemStack {
    val meta = itemMeta
    meta.addItemFlags(*flag)
    itemMeta = meta
    return this
}