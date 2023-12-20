package me.hugo.thankmas.gui.view

import me.hugo.thankmas.gui.Menu
import me.hugo.thankmas.gui.StatefulIcon
import me.hugo.thankmas.state.StatefulValue
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.koin.core.component.KoinComponent

/** Specific menu view of [menu] for [player]. */
public class MenuView(private val player: Player, public val menu: Menu) : KoinComponent {

    private val callbacks: MutableMap<StatefulIcon<*>, (old: Any?, new: Any?, value: StatefulValue<out Any?>) -> Unit> =
        mutableMapOf()

    public val inventory: Inventory = menu.buildInventory(player, this)

    public fun subscribeRebuild(slot: Int, icon: StatefulIcon<*>) {
        val callback: (old: Any?, new: Any?, value: StatefulValue<out Any?>) -> Unit =
            { _, _, _ -> rebuildIcon(slot) }

        callbacks[icon] = callback
        icon.value.subscribe(callback)
    }

    /** Rebuilds the icon in slot [slot] for this menu view. */
    private fun rebuildIcon(slot: Int) {
        val icon = menu.getIconOrNull(slot)
        requireNotNull(icon) { "Tried to rebuild icon on empty slot." }

        inventory.setItem(slot, icon.itemSupplier(player))
    }

    /** Runs when this menu view is closed. */
    public fun onClose() {
        callbacks.forEach { it.key.value.unsubscribe(it.value) }
    }
}