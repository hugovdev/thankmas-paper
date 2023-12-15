package me.hugo.thankmas.gui

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * Menu item that runs the actions in [actions] when clicked.
 * Display item is supplied through the [itemSupplier].
 */
public class Icon(
    public val actions: MutableList<(player: Player, clickedSlot: Int, clickedItem: ItemStack) -> Unit> = mutableListOf(),
    public val itemSupplier: (player: Player) -> ItemStack
)