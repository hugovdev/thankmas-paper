package me.hugo.thankmas.gui

import dev.kezz.miniphrase.MiniPhrase
import me.hugo.thankmas.gui.view.MenuView
import me.hugo.thankmas.listener.MenuManager
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Translatable menu with clickable icons and a default format. */
public open class Menu(
    private val menuFrames: List<Pair<Int, String>>,
    private val size: Int,
    private val icons: MutableMap<Int, Icon> = mutableMapOf(),
    public val menuFormat: MenuFormat? = null,
    private val miniPhrase: MiniPhrase
) : KoinComponent {

    public constructor(
        titleKey: String, size: Int, icons: MutableMap<Int, Icon>, menuFormat: MenuFormat?,
        miniPhrase: MiniPhrase
    ) :
            this(listOf(Pair(0, titleKey)), size, icons, menuFormat, miniPhrase)

    public constructor(
        config: FileConfiguration,
        path: String,
        miniPhrase: MiniPhrase
    ) : this(
        listOf(Pair(0, config.getString("$path.title") ?: "$path.title")),
        config.getInt("$path.size", 9 * 3),
        menuFormat = config.getString("$path.format")?.uppercase()?.let { MenuFormat.valueOf(it) },
        miniPhrase = miniPhrase
    )

    private val titleKey: String
        get() = menuFrames[0].second

    public val frames: Int = menuFrames.size

    /** Gets the frame in position [index]. */
    public fun getFrameEntry(index: Int): Pair<Int, String> = menuFrames[index]

    /** Creates an inventory view for [player] and opens it. */
    public fun open(player: Player, animated: Boolean = true) {
        val menuView = MenuView(player, this, miniPhrase)

        // Jump into the last frame to prevent the menu from
        // being animated!
        if (!animated) menuView.currentTitleFrame = frames

        val menuManager: MenuManager by inject()
        menuManager.register(menuView.inventory, menuView)

        menuView.open()
    }

    /** Build this menu for [player]. */
    public fun buildInventory(player: Player, view: MenuView, miniPhrase: MiniPhrase = this.miniPhrase): Inventory {
        val inventory = Bukkit.createInventory(null, size, miniPhrase.translate(titleKey, player.locale()))

        icons.forEach {
            val icon = it.value

            inventory.setItem(it.key, icon.itemSupplier(player))
            if (icon is StatefulIcon<*>) view.subscribeRebuild(it.key, icon)
        }

        return inventory
    }

    /**
     * Follows the [menuFormat] and adds an icon in the first
     * empty slot that belongs to the character [char].
     *
     * @returns whether the icon was added or not.
     */
    public fun addIcon(icon: Icon, char: Char = 'X'): Boolean {
        requireNotNull(menuFormat) { "Tried to add icon to menu $titleKey without a menu format." }

        val firstEmptySlot = menuFormat.getSlotsForChar(char).firstOrNull { !icons.containsKey(it) } ?: return false
        setIcon(firstEmptySlot, icon)

        return true
    }

    /**
     * Add the icon [icon] to the slot [slot].
     *
     * @returns last icon in [slot] before possibly
     * getting overridden by [icon].
     */
    public fun setIcon(slot: Int, icon: Icon): Icon? = icons.put(slot, icon)

    /**
     * Add the icon [icon] to the slots [slots].
     */
    public fun setIcons(icon: Icon, vararg slots: Int): Unit = slots.forEach { slot -> icons[slot] = icon }

    /**
     * Gets the icon in the slot [slot] if existent.
     */
    public fun getIconOrNull(slot: Int): Icon? = icons[slot]

    /** Returns whether this menu lacks space for more icons in the [char] slots. */
    public fun isFull(char: Char): Boolean {
        requireNotNull(menuFormat) { "Tried to addIcon to Menu without a page format." }

        return menuFormat.getSlotsForChar(char).all { icons[it] != null }
    }

    /** @returns all the icons in this menu. */
    public fun getIcons(): List<Pair<Int, Icon>> {
        return icons.toList()
    }

    /**
     * A way to organize items in of different categories
     * in inventories.
     */
    public enum class MenuFormat(private val itemDistribution: String) {
        ONE_ROW(
            "---------"
                    + "-XXXXXXX-"
                    + "---PIN---"
        ),
        ONE_TRIMMED(
            ("---------"
                    + "--XXXXX--"
                    + "---PIN---")
        ),
        JOURNAL(
            ("X-X---X-X" +
                    "---------" +
                    "X-X---X-X" +
                    "---------" +
                    "X-X---X-X" +
                    "---PIN---")
        ),
        TWO_ROWS(
            ("---------"
                    + "-XXXXXXX-"
                    + "-XXXXXXX-"
                    + "---PIN---")
        ),
        GAME_SELECTOR(
            ("SSS---CCC"
                    + "SSS---CCC"
                    + "SSS---CCC")
        ),
        ROD_UPGRADES(
            ("---------"
                    + "X_X_X_X_X"
                    + "---------")
        ),
        FISH_TRACKER(
            ("PXXXXXXXN"
                    + "PXXXXXXXN"
                    + "PXXXXXXXN")
        ),
        STK_SHOP(
            ("---------"
                    + "---------"
                    + "--XXXXX--"
                    + "--XXXXX--"
                    + "--XXXXX--"
                    + "--XXXXX--")
        ),
        THREE_ROWS(
            ("---------"
                    + "-XXXXXXX-"
                    + "-XXXXXXX-"
                    + "-XXXXXXX-"
                    + "---PIN---")
        ),
        THREE_ROWS_TRIMMED(
            ("---------"
                    + "--XXXXX--"
                    + "--XXXXX--"
                    + "--XXXXX--"
                    + "---PIN---")
        ),
        FOUR_SLIM_ROWS(
            "---XXX---" +
                    "---XXX---" +
                    "---XXX---" +
                    "---XXX---" +
                    "---P-N---" +
                    "---------"
        ), ;

        public fun getSlotsForChar(char: Char = 'X'): List<Int> =
            itemDistribution.toCharArray()
                .withIndex()
                .filter { it.value == char }
                .map { it.index }

        public fun getSlotForChar(char: Char = 'X'): Int =
            itemDistribution.toCharArray().indexOfFirst { it == char }
    }

}