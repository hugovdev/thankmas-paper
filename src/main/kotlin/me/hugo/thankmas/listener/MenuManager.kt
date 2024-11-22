package me.hugo.thankmas.listener

import io.papermc.paper.adventure.PaperAdventure
import me.hugo.thankmas.ThankmasPlugin
import me.hugo.thankmas.gui.Icon
import me.hugo.thankmas.gui.view.MenuView
import me.hugo.thankmas.lang.Translated
import me.hugo.thankmas.player.translate
import me.hugo.thankmas.registry.MapBasedRegistry
import net.kyori.adventure.text.Component
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
import net.minecraft.server.level.ServerPlayer
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.entity.CraftHumanEntity
import org.bukkit.craftbukkit.inventory.CraftContainer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryView
import org.koin.core.annotation.Single

/**
 * Enables menu icons by listening to the InventoryClickEvent
 * and casting inventory holders to [MenuView].
 */
@Single
public class MenuManager : MapBasedRegistry<Inventory, MenuView>(), Listener, Translated {

    init {
        Bukkit.getScheduler().runTaskTimerAsynchronously(ThankmasPlugin.instance(), Runnable {
            val openedViews = iterator()

            while (openedViews.hasNext()) {
                val view = openedViews.next()

                val menuView = view.value
                val menu = menuView.menu

                if (menuView.currentTitleFrame >= menu.frames) continue

                val nextFrame = menu.getFrameEntry(menuView.currentTitleFrame)

                if (menuView.currentTitleTick >= nextFrame.first) {
                    menuView.inventoryView?.setTitle(menuView.player.translate(nextFrame.second))

                    menuView.currentTitleFrame++
                }

                menuView.currentTitleTick++
            }

        }, 0L, 1L)
    }

    private fun InventoryView.setTitle(title: Component) {
        val vanillaTitle = PaperAdventure.asVanilla(title)

        val entityPlayer = (player as CraftHumanEntity).handle as ServerPlayer
        val containerId = entityPlayer.containerMenu.containerId
        val windowType = CraftContainer.getNotchInventoryType(topInventory)
        entityPlayer.connection.send(ClientboundOpenScreenPacket(containerId, windowType, vanillaTitle))
        (player as Player).updateInventory()
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as Player
        val menuView = getOrNull(event.view.topInventory) ?: return

        event.isCancelled = true

        if (event.clickedInventory != menuView.inventory) return

        val slot = event.rawSlot

        menuView.menu.getIconOrNull(slot)?.actions
            ?.forEach { action ->
                action(
                    Icon.IconClickContext(player, slot, event.currentItem, event.click),
                    menuView
                )
            }
    }

    @EventHandler
    private fun onInventoryClose(event: InventoryCloseEvent) {
        val menu = getOrNull(event.inventory) ?: return

        menu.onClose()
        remove(menu.inventory)
    }

}