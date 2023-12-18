package me.hugo.thankmas.items.clickable

import me.hugo.thankmas.config.getStringNotNull
import me.hugo.thankmas.items.*
import me.hugo.thankmas.lang.TranslatedComponent
import org.bukkit.NamespacedKey
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

/** Item that can be clicked to run a command. */
public class ClickableItem : TranslatedComponent {

    public companion object {
        /** Key used to identify which items stacks are clickable and what action they should run. */
        public val CLICKABLE_ITEM_ID: NamespacedKey = NamespacedKey("thankmas", "clickable_item_id")
    }

    /** This clickable item's id. */
    public val id: String

    /** Cached item stacks for each language. */
    private val items: MutableMap<Locale, ItemStack> = mutableMapOf()

    /** Default slot where this item gets given. */
    private val defaultSlot: Int?

    /**
     * Action that runs when the item is clicked.
     * @returns whether the interaction event is cancelled.
     */
    public val clickAction: (clicker: Player, action: Action) -> Boolean

    /** Constructor that loads the item and command to run on click. */
    public constructor(id: String, config: FileConfiguration, path: String) {
        this.id = id

        val command = config.getStringNotNull("$path.command")
        this.clickAction = { player, action ->
            if (action.isRightClick) player.chat("/$command")
            true
        }

        this.defaultSlot = config.getInt("$path.slot")

        createItems(config, path)
    }

    /** Constructor that lets you define a custom click action for the item. */
    public constructor(
        id: String,
        config: FileConfiguration,
        path: String,
        clickAction: (clicker: Player, action: Action) -> Boolean
    ) {
        this.id = id
        this.clickAction = clickAction
        this.defaultSlot = config.getInt("$path.slot")

        createItems(config, path)
    }

    /** Loads an item stack from [config] at [path]. */
    private fun createItems(config: FileConfiguration, path: String) {
        val translatableItem = TranslatableItem(config, path)

        miniPhrase.translationRegistry.getLocales().forEach {
            // Create the item from config specifications and set the
            // clickable item id on the PDC.
            val item = translatableItem.buildItem(it)
                .setKeyedData(CLICKABLE_ITEM_ID, PersistentDataType.STRING, id)

            // Cache the item stack in each language.
            this.items[it] = item
        }
    }

    /** Give to [player] this item. */
    public fun give(player: Player, slot: Int? = this.defaultSlot) {
        requireNotNull(slot) { "Tried to give clickable item $id but default slot is null and there was no slot specified." }

        val item = items[player.locale()] ?: return
        player.inventory.setItem(slot, item)
    }

}