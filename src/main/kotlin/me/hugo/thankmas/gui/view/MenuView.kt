package me.hugo.thankmas.gui.view

import dev.kezz.miniphrase.MiniPhrase
import me.hugo.thankmas.gui.Menu
import me.hugo.thankmas.gui.StatefulIcon
import me.hugo.thankmas.state.StatefulValue
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryView
import org.koin.core.component.KoinComponent

/** Specific menu view of [menu] for [player]. */
public class MenuView(public val player: Player, public val menu: Menu, public val miniPhrase: MiniPhrase) :
    KoinComponent {

    private val callbacks: MutableMap<StatefulValue<*>, (old: Any?, new: Any?, value: StatefulValue<out Any?>) -> Unit> =
        mutableMapOf()

    public val inventory: Inventory = menu.buildInventory(player, this, miniPhrase)

    public var inventoryView: InventoryView? = null
        private set

    public var currentTitleFrame: Int = 1
    public var currentTitleTick: Int = 0

    public fun open() {
        inventoryView = player.openInventory(inventory)
    }

    public fun subscribeRebuild(slot: Int, icon: StatefulIcon<*>) {
        val callback: (old: Any?, new: Any?, value: StatefulValue<out Any?>) -> Unit =
            { _, _, _ -> rebuildIcon(slot) }

        val valueToListen = icon.value(player)

        callbacks[valueToListen] = callback
        valueToListen.subscribe(callback)
    }

    /** Rebuilds the icon in slot [slot] for this menu view. */
    private fun rebuildIcon(slot: Int) {
        val icon = menu.getIconOrNull(slot)
        requireNotNull(icon) { "Tried to rebuild icon on empty slot." }

        inventory.setItem(slot, icon.itemSupplier(player))
    }

    /** Runs when this menu view is closed. */
    public fun onClose() {
        callbacks.forEach { it.key.unsubscribe(it.value) }
    }
}