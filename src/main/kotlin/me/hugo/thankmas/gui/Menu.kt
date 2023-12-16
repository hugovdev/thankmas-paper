package me.hugo.thankmas.gui

import me.hugo.thankmas.lang.TranslatedComponent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory

/** Translatable menu with clickable icons and a default format. */
public open class Menu(
    private val titleKey: String,
    private val size: Int,
    private val icons: MutableMap<Int, Icon> = mutableMapOf(),
    private val menuFormat: MenuFormat? = null
) : TranslatedComponent {

    /** Creates an inventory view for [player] and opens it. */
    public fun open(player: Player) {
        val menuView = MenuView(player, this)
        player.openInventory(menuView.inventory)
    }

    /** Build this menu for [player]. */
    public fun buildInventory(player: Player, menuView: MenuView): Inventory {
        val inventory = Bukkit.createInventory(menuView, size, miniPhrase.translate(titleKey, player.locale()))
        icons.forEach { inventory.setItem(it.key, it.value.itemSupplier(player)) }

        return inventory
    }

    /**
     * Follows the [menuFormat] and adds an icon in the first empty
     * slot that belongs to the character [char].
     *
     * @returns whether the icon was added or not.
     */
    public fun addIcon(icon: Icon, char: Char = 'X'): Boolean {
        requireNotNull(menuFormat) { "Tried to addIcon to Menu without a page format." }

        val firstEmptySlot = menuFormat.getSlotsForChar(char).firstOrNull { icons[it] == null } ?: return false
        icons[firstEmptySlot] = icon

        return true
    }

    /**
     * Add the icon [icon] to the slot [slot].
     */
    public fun setIcon(slot: Int, icon: Icon) {
        icons[slot] = icon
    }

    /**
     * Gets the icon in the slot [slot] if existent.
     */
    public fun getIconOrNull(slot: Int): Icon? = icons[slot]

    /** Returns whether this menu lacks space for more icons in the [char] slots. */
    public fun isFull(char: Char): Boolean {
        requireNotNull(menuFormat) { "Tried to addIcon to Menu without a page format." }

        return menuFormat.getSlotsForChar(char).all { icons[it] != null }
    }

    /**
     * A way to organize items in of different categories
     * in inventories.
     */
    public enum class MenuFormat(private val itemDistribution: String) {
        ONE_ROW(
            "---------"
                    + "-XXXXXXX-"
        ),
        ONE_TRIMMED(
            ("---------"
                    + "--XXXXX--")
        ),
        TWO_ROWS(
            ("---------"
                    + "-XXXXXXX-"
                    + "-XXXXXXX-")
        ),
        TWO_ROWS_TRIMMED(
            ("---------"
                    + "--XXXXX--"
                    + "--XXXXX--")
        ),
        THREE_ROWS(
            ("---------"
                    + "-XXXXXXX-"
                    + "-XXXXXXX-"
                    + "-XXXXXXX-")
        ),
        THREE_ROWS_TRIMMED(
            ("---------"
                    + "--XXXXX--"
                    + "--XXXXX--"
                    + "--XXXXX--")
        );

        public fun getSlotsForChar(char: Char = 'X'): List<Int> =
            itemDistribution.toCharArray()
                .withIndex()
                .filter { it.value == char }
                .map { it.index }

        public fun getSlotForChar(char: Char = 'X'): Int =
            itemDistribution.toCharArray().indexOfFirst { it == char }
    }

}