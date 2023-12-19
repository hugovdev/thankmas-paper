package me.hugo.thankmas.gui

import me.hugo.thankmas.gui.view.MenuView
import me.hugo.thankmas.state.StatefulValue
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

/**
 * Menu item that runs [actions] when clicked.
 * Display item is supplied through the [itemSupplier].
 */
public open class Icon {

    /** Actions that run when the icon is clicked. */
    public val actions: MutableList<(iconClickContext: IconClickContext, menuView: MenuView) -> Unit> = mutableListOf()

    /** Supplier for the item that composes this icon. */
    public val itemSupplier: (player: Player) -> ItemStack?

    /** Constructor that allows for multiple action input. */
    public constructor(
        actions: MutableList<(iconClickContext: IconClickContext, menuView: MenuView) -> Unit> = mutableListOf(),
        itemSupplier: (player: Player) -> ItemStack?
    ) {
        this.actions.addAll(actions)
        this.itemSupplier = itemSupplier
    }

    /** Constructor with a single click action. */
    public constructor(
        action: (iconClickContext: IconClickContext, menuView: MenuView) -> Unit,
        itemSupplier: (player: Player) -> ItemStack?
    ) {
        this.actions.add(action)
        this.itemSupplier = itemSupplier
    }

    /** Transforms this icon into a Stateful Icon. */
    public fun <T> listen(value: StatefulValue<T>): StatefulIcon<T> {
        return StatefulIcon(value, actions, itemSupplier)
    }

    /** Stores context to an inventory click. */
    public data class IconClickContext(
        val clicker: Player,
        val clickedSlot: Int,
        val clickedItem: ItemStack?,
        val clickType: ClickType
    )
}