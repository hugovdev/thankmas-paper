package me.hugo.thankmas.gui

import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

/**
 * Menu item that runs [actions] when clicked.
 * Display item is supplied through the [itemSupplier].
 */
public class Icon {

    public val actions: MutableList<(iconClickContext: IconClickContext, menuView: MenuView) -> Unit> = mutableListOf()
    public val itemSupplier: (player: Player) -> ItemStack?

    /**
     * Constructor that allows for multiple action input.
     */
    public constructor(
        actions: MutableList<(iconClickContext: IconClickContext, menuView: MenuView) -> Unit> = mutableListOf(),
        itemSupplier: (player: Player) -> ItemStack?
    ) {
        this.actions.addAll(actions)
        this.itemSupplier = itemSupplier
    }

    /**
     * Constructor with a single click action.
     */
    public constructor(
        action: (iconClickContext: IconClickContext, menuView: MenuView) -> Unit,
        itemSupplier: (player: Player) -> ItemStack?
    ) {
        this.actions.add(action)
        this.itemSupplier = itemSupplier
    }

    /** Stores context to an inventory click. */
    public data class IconClickContext(
        val clicker: Player,
        val clickedSlot: Int,
        val clickedItem: ItemStack?,
        val clickType: ClickType
    )
}