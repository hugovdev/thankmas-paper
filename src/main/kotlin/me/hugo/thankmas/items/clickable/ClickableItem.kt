package me.hugo.thankmas.items.clickable

import me.hugo.thankmas.config.string
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

    /** Item to be given! */
    private val translatableItem: TranslatableItem

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

        val command = config.string("$path.command")
        this.clickAction = { player, action ->
            if (action.isRightClick) player.chat("/$command")
            true
        }

        this.defaultSlot = config.getInt("$path.slot")

        this.translatableItem = TranslatableItem(config, path)
        this.translatableItem.editBaseItem { it.setKeyedData(CLICKABLE_ITEM_ID, PersistentDataType.STRING, id) }
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

        this.translatableItem = TranslatableItem(config, path)
        this.translatableItem.editBaseItem { it.setKeyedData(CLICKABLE_ITEM_ID, PersistentDataType.STRING, id) }
    }

    /** Give to [player] this item. */
    public fun give(player: Player, locale: Locale? = null, slot: Int? = this.defaultSlot) {
        requireNotNull(slot) { "Tried to give clickable item $id but default slot is null and there was no slot specified." }

        player.inventory.setItem(slot, translatableItem.buildItem(locale ?: player.locale()))
    }

}