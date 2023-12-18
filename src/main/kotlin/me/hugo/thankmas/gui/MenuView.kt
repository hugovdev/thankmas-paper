package me.hugo.thankmas.gui

import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.koin.core.component.KoinComponent

/** Specific menu view of [menu] for [player]. */
public class MenuView(private val player: Player, public val menu: Menu) : KoinComponent {

    public val inventory: Inventory = menu.buildInventory(player)

    /** Rebuilds the icon in slot [slot] for this menu view. */
    public fun rebuildIcon(slot: Int) {
        val icon = menu.getIconOrNull(slot)
        requireNotNull(icon) { "Tried to rebuild icon on empty slot." }

        inventory.setItem(slot, icon.itemSupplier(player))
    }
}