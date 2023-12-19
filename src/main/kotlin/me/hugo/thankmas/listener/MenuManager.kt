package me.hugo.thankmas.listener

import me.hugo.thankmas.gui.Icon
import me.hugo.thankmas.gui.view.MenuView
import me.hugo.thankmas.registry.MapBasedRegistry
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.koin.core.annotation.Single

/**
 * Enables menu icons by listening to the InventoryClickEvent
 * and casting inventory holders to [MenuView].
 */
@Single
public class MenuManager : MapBasedRegistry<Inventory, MenuView>(), Listener {

    @EventHandler
    private fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as Player
        val menuView = getOrNull(event.view.topInventory) ?: return

        event.isCancelled = true

        if (event.clickedInventory != menuView.inventory) return

        val slot = event.rawSlot

        menuView.menu.getIconOrNull(slot)?.actions
            ?.forEach { action -> action(Icon.IconClickContext(player, slot, event.currentItem, event.click), menuView) }
    }

    @EventHandler
    private fun onInventoryClose(event: InventoryCloseEvent) {
        val menu = getOrNull(event.inventory) ?: return

        menu.onClose()
        remove(menu.inventory)
    }

}