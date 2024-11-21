package me.hugo.thankmas.font

import net.kyori.adventure.text.TextComponent

/**
 * Returns the rough length of all the characters
 * in this TextComponent. (In the default Minecraft font)
 */
public val TextComponent.width: Int
    get() = content().sumOf { DefaultFontInfo.getDefaultFontInfo(it).length }
