package me.hugo.thankmas.items.clickable

import me.hugo.thankmas.items.getKeyedData
import me.hugo.thankmas.items.hasKeyedData
import me.hugo.thankmas.registry.MapBasedRegistry
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.persistence.PersistentDataType
import org.koin.core.annotation.Single

/** Registers clickable items to make them clickable through listeners. */
@Single
public class ClickableItemRegistry : MapBasedRegistry<String, ClickableItem>(), Listener {

    @EventHandler
    private fun onItemClickInventory(event: InventoryClickEvent) {
        if (event.whoClicked.gameMode == GameMode.CREATIVE) return
        val item = event.currentItem ?: return

        val clickableItemId =
            item.getKeyedData(ClickableItem.CLICKABLE_ITEM_ID, PersistentDataType.STRING) ?: return

        event.isCancelled = true
        get(clickableItemId).clickAction(event.whoClicked as Player, Action.RIGHT_CLICK_AIR)
    }

    @EventHandler
    private fun onItemClick(event: PlayerInteractEvent) {
        if (event.action == Action.PHYSICAL) return
        val item = event.item ?: return

        val clickableItemId =
            item.getKeyedData(ClickableItem.CLICKABLE_ITEM_ID, PersistentDataType.STRING) ?: return

        event.isCancelled = get(clickableItemId).clickAction(event.player, event.action)
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