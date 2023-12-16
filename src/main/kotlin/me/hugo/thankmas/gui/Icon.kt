package me.hugo.thankmas.gui

import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

/**
 * Menu item that runs the actions in [actions] when clicked.
 * Display item is supplied through the [itemSupplier].
 */
public class Icon(
    public val actions: MutableList<(iconClickContext: IconClickContext, menuView: MenuView) -> Unit> = mutableListOf(),
    public val itemSupplier: (player: Player) -> ItemStack
) {
    /** Stores context to an inventory click. */
    public data class IconClickContext(
        val clicker: Player,
        val clickedSlot: Int,
        val clickedItem: ItemStack?,
        val clickType: ClickType
    )
}