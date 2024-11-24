package me.hugo.thankmas.gui

import dev.kezz.miniphrase.MiniPhrase
import me.hugo.thankmas.ThankmasPlugin
import me.hugo.thankmas.config.ConfigurationProvider
import me.hugo.thankmas.config.enum
import me.hugo.thankmas.config.string
import me.hugo.thankmas.items.TranslatableItem
import me.hugo.thankmas.player.playSound
import org.bukkit.Sound
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Collection of menus with page navigation.
 */
public open class PaginatedMenu(
    private val menuFrames: List<Pair<Int, String>>,
    private val size: Int,
    private val menuFormat: Menu.MenuFormat,
    private val representativeIcon: TranslatableItem? = null,
    private val lastMenu: Menu? = null,
    private val miniPhrase: MiniPhrase
) {

    public constructor(config: FileConfiguration, path: String, lastMenu: Menu? = null, miniPhrase: MiniPhrase) : this(
        config.string("$path.title"),
        config.getInt("$path.size", 9 * 3),
        config.enum<Menu.MenuFormat>("$path.format"),
        config.getConfigurationSection("$path.icon")?.let { TranslatableItem(config, "$path.icon") },
        lastMenu,
        miniPhrase
    )

    public constructor(
        titleKey: String,
        size: Int,
        menuFormat: Menu.MenuFormat,
        representativeIcon: TranslatableItem? = null,
        lastMenu: Menu? = null,
        miniPhrase: MiniPhrase
    ) : this(listOf(Pair(0, titleKey)), size, menuFormat, representativeIcon, lastMenu, miniPhrase)

    public companion object : KoinComponent {
        private val configProvider: ConfigurationProvider by inject()

        /** Gets the icon with [id] from the menu icons configuration using global translations. */
        private fun globalIcon(id: String): TranslatableItem = TranslatableItem(
            configProvider.getOrLoad("global/menu_icons.yml"), id,
            ThankmasPlugin.instance().globalTranslations
        )

        public val EXIT: TranslatableItem = globalIcon("exit")
        public val PREVIOUS_MENU: TranslatableItem = globalIcon("previous-menu")
        public val PREVIOUS_PAGE: TranslatableItem = globalIcon("previous-page")
        public val NEXT_PAGE: TranslatableItem = globalIcon("next-page")
    }

    private var currentIndex: Int = -1
    private var pages: MutableList<Menu> =
        mutableListOf(Menu(menuFrames, size, menuFormat = menuFormat, miniPhrase = miniPhrase))

    public val pageList: List<Menu>
        get() = pages.toList()

    init {
        createNewPage()
    }

    /** Opens this paginated menu to [player]. */
    public fun open(player: Player) {
        pages.first().open(player)
    }

    /**
     * Sets the icon in [slot] in page [page].
     */
    public fun setIcon(slot: Int, page: Int, icon: Icon) {
        val menu = pages.getOrNull(page)
        requireNotNull(menu) { "Cannot change icon in non-existent page $page." }

        menu.setIcon(slot, icon)
    }

    /**
     * Adds this icon to the current page or the next one if needed.
     * @returns whether a new page was created.
     */
    public fun addIcon(icon: Icon, char: Char = 'X'): Boolean {
        if (lastPage().addIcon(icon, char)) return false

        createNewPage()
        addIcon(icon, char)

        return true
    }

    /** Adds a new page. */
    private fun createNewPage(): Menu {
        val newPage = Menu(menuFrames, size, menuFormat = menuFormat, miniPhrase = miniPhrase)

        pages.add(newPage)
        currentIndex++

        addPaginationButtons()

        return newPage
    }

    /** Adds pagination buttons to the new added page and updates the last page's buttons. */
    private fun addPaginationButtons() {
        val newPage = lastPage()

        representativeIcon?.let { icon ->
            newPage.setIcon(menuFormat.getSlotForChar('I'), Icon {
                icon.buildItem(it.locale())
            })
        }

        if (currentIndex > 0) {
            val nextPageValue = currentIndex + 1

            pages[currentIndex - 1].setIcon(
                menuFormat.getSlotForChar('N'),
                Icon({ context, _ ->
                    val clicker = context.clicker

                    newPage.open(clicker, false)
                    clicker.playSound(Sound.BLOCK_WOODEN_BUTTON_CLICK_ON)
                }) {
                    NEXT_PAGE.buildItem(it.locale()) {
                        parsed("page", nextPageValue)
                    }
                })

            val lastIndex = currentIndex - 1

            val previousPage = currentIndex
            newPage.setIcon(
                menuFormat.getSlotForChar('P'),
                Icon({ context, _ ->
                    val clicker = context.clicker

                    pages[lastIndex].open(clicker, false)
                    clicker.playSound(Sound.BLOCK_WOODEN_BUTTON_CLICK_ON)
                }) {
                    PREVIOUS_PAGE.buildItem(it.locale()) {
                        parsed("page", previousPage)
                    }
                })
        } else {
            if (lastMenu == null) {
                newPage.setIcon(menuFormat.getSlotForChar('P'), Icon({ context, _ ->
                    val clicker = context.clicker

                    clicker.closeInventory()
                    clicker.playSound(Sound.BLOCK_WOODEN_BUTTON_CLICK_ON)
                }) { EXIT.buildItem(it.locale()) })
            } else {
                newPage.setIcon(menuFormat.getSlotForChar('P'), Icon({ context, _ ->
                    val clicker = context.clicker

                    lastMenu.open(clicker)
                    clicker.playSound(Sound.BLOCK_WOODEN_BUTTON_CLICK_ON)
                }) { PREVIOUS_MENU.buildItem(it.locale()) })
            }
        }
    }

    /** @returns the first page. */
    public fun firstPage(): Menu {
        return pages.first()
    }

    /** @returns the last page. */
    private fun lastPage(): Menu {
        return pages[currentIndex]
    }

}