package me.hugo.thankmas.gui

import me.hugo.thankmas.gui.view.MenuView
import me.hugo.thankmas.state.StatefulValue
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

public class StatefulIcon<T> : Icon {

    /** Value that this icon listens to. */
    public val value: StatefulValue<T>

    /** Constructor that allows for multiple action input. */
    public constructor(
        value: StatefulValue<T>,
        actions: MutableList<(iconClickContext: IconClickContext, menuView: MenuView) -> Unit> = mutableListOf(),
        itemSupplier: (player: Player) -> ItemStack?
    ) : super(actions, itemSupplier) {
        this.value = value
    }

    /** Constructor with a single click action. */
    public constructor(
        value: StatefulValue<T>,
        action: (iconClickContext: IconClickContext, menuView: MenuView) -> Unit,
        itemSupplier: (player: Player) -> ItemStack?
    ) : super(action, itemSupplier) {
        this.value = value
    }

}