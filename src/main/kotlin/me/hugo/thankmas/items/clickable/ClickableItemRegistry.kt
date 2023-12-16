package me.hugo.thankmas.items.clickable

import me.hugo.thankmas.items.getKeyedData
import me.hugo.thankmas.items.hasKeyedData
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.persistence.PersistentDataType
import org.koin.core.annotation.Single

/** Registers clickable items to make them clickable through listeners. */
@Single
public class ClickableItemRegistry : Listener {

    // ClickableItem id -> ClickableItem
    private val clickableItems: MutableMap<String, ClickableItem> = mutableMapOf()

    /** Registers [item] to make it clickable. */
    public fun registerItem(item: ClickableItem) {
        clickableItems[item.id] = item
    }

    /** @returns the clickable item for this [id], can be null. */
    public fun getItemOrNull(id: String): ClickableItem? {
        return clickableItems[id]
    }

    /** @returns the clickable item for this [id]. */
    public fun getItem(id: String): ClickableItem {
        val item = getItemOrNull(id)
        requireNotNull(item) { "Tried to get clickable item with id \"$id\" but it's null." }

        return item
    }

    @EventHandler
    private fun onItemClick(event: PlayerInteractEvent) {
        if (event.action == Action.PHYSICAL) return
        val item = event.item ?: return

        val clickableItemId =
            item.getKeyedData(ClickableItem.CLICKABLE_ITEM_ID, PersistentDataType.STRING) ?: return

        event.isCancelled = getItem(clickableItemId).clickAction(event.player, event.action)
    }

    @EventHandler
    private fun onItemSwap(event: PlayerSwapHandItemsEvent) {
        if (event.mainHandItem.hasKeyedData(ClickableItem.CLICKABLE_ITEM_ID, PersistentDataType.STRING) ||
            event.offHandItem.hasKeyedData(ClickableItem.CLICKABLE_ITEM_ID, PersistentDataType.STRING)
        ) {
            event.isCancelled = true
            return
        }
    }
}