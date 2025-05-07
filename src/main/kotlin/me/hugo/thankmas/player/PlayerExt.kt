package me.hugo.thankmas.player

import dev.kezz.miniphrase.MiniPhraseContext
import dev.kezz.miniphrase.tag.TagResolverBuilder
import me.hugo.thankmas.ThankmasPlugin
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound.Source
import net.kyori.adventure.sound.Sound.sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.*

public fun UUID?.player(): Player? = this?.let { Bukkit.getPlayer(it)?.takeIf { it.isOnline } }

context(context: MiniPhraseContext)
public fun Player.translate(
    key: String,
    locale: Locale? = null,
    tags: (TagResolverBuilder.() -> Unit)? = null
): Component =
    context.miniPhrase.translate(key, locale ?: this.locale(), tags)

context(context: MiniPhraseContext)
public fun Player.translateList(
    key: String,
    locale: Locale? = null, tags: (TagResolverBuilder.() -> Unit)? = null
): List<Component> =
    context.miniPhrase.translateList(key, locale ?: this.locale(), tags)

public fun Inventory.firstIf(predicate: (ItemStack) -> Boolean): Pair<Int, ItemStack>? {
    for (slot in 0 until size) {
        val item = getItem(slot) ?: continue
        if (predicate(item)) return Pair(slot, item)
    }

    return null
}

context(context: MiniPhraseContext)
public fun Player.showTitle(key: String, times: Title.Times, tags: (TagResolverBuilder.() -> Unit)? = null) {
    val titles = context.miniPhrase.translateList(key, this.locale(), tags)

    if (titles.size > 1) {
        val title = titles.first()
        val subtitle = titles[1]

        showTitle(Title.title(title, subtitle, times))
    } else showTitle(Title.title(titles.first(), Component.empty(), times))
}

/** @returns every online player with an active scoreboard. */
public fun playersWithBoard(): List<Player> {
    return Bukkit.getOnlinePlayers()
        .filter {
            ThankmasPlugin.instance<ThankmasPlugin<*>>().playerDataManager.getPlayerDataOrNull(it.uniqueId)?.getBoardOrNull() != null
        }
}

/** Updates this player's board lines that contains [tags]. */
public fun Player.updateBoardTags(vararg tags: String) {
    val scoreboardManager = ThankmasPlugin.instance<ThankmasPlugin<*>>().scoreboardTemplateManager
    val playerData = ThankmasPlugin.instance<ThankmasPlugin<*>>().playerDataManager.getPlayerData(uniqueId)

    playerData.getBoardOrNull() ?: return

    scoreboardManager.getTemplate(playerData.lastBoardId).updateLinesForTag(this, *tags)
}

/** Updates this player's board lines that contains [tags]. */
public fun updateBoardTags(vararg tags: String) {
    playersWithBoard().forEach { it.updateBoardTags(*tags) }
}

public fun Player.playSound(sound: Sound): Unit = playSound(location, sound, 1.0f, 1.0f)

public fun Player.playSound(sound: String, source: Source = Source.AMBIENT): Unit =
    playSound(sound(Key.key(sound), source, 1.0f, 1.0f))

public fun Player.stopSound(sound: String, source: Source = Source.AMBIENT): Unit =
    stopSound(sound(Key.key(sound), source, 1.0f, 1.0f))

public fun Player.reset(gameMode: GameMode) {
    setGameMode(gameMode)
    health = getAttribute(Attribute.MAX_HEALTH)?.baseValue ?: 20.0
    foodLevel = 20
    exp = 0.0f
    level = 0
    arrowsInBody = 0

    fireTicks = 0

    closeInventory()

    inventory.clear()
    inventory.armorContents = arrayOf()

    inventory.heldItemSlot = 0

    activePotionEffects.forEach { removePotionEffect(it.type) }
}