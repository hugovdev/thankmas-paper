package me.hugo.thankmas.listener

import me.hugo.thankmas.gui.Icon
import me.hugo.thankmas.gui.MenuView
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

public class MenuListener : Listener {

    @EventHandler
    private fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as Player
        val menu = event.view.topInventory.holder as? MenuView ?: return

        event.isCancelled = true

        if (event.clickedInventory != menu.inventory) return

        val slot = event.slot

        menu.menu.getIconOrNull(slot)?.actions
            ?.forEach { action -> action(Icon.ClickContext(player, slot, event.currentItem, event.click), menu) }
    }

}