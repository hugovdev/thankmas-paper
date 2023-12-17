package me.hugo.thankmas.gui.paginated

import me.hugo.thankmas.ThankmasPlugin
import me.hugo.thankmas.config.ConfigurationProvider
import me.hugo.thankmas.gui.Icon
import me.hugo.thankmas.gui.Menu
import me.hugo.thankmas.items.TranslatableItem
import me.hugo.thankmas.player.playSound
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Collection of menus with page navigation.
 */
public open class PaginatedMenu(
    private val titleKey: String,
    private val size: Int,
    private val menuFormat: Menu.MenuFormat,
    private val representativeIcon: TranslatableItem? = null
) {

    public companion object : KoinComponent {
        private val configProvider: ConfigurationProvider by inject()

        public val EXIT: TranslatableItem = TranslatableItem(
            configProvider.getOrLoad("menu_icons", "../global/"),
            "exit",
            ThankmasPlugin.instance().globalTranslations
        )
        public val PREVIOUS_PAGE: TranslatableItem =
            TranslatableItem(
                configProvider.getOrLoad("menu_icons", "../global/"),
                "previous-page",
                ThankmasPlugin.instance().globalTranslations
            )
        public val NEXT_PAGE: TranslatableItem = TranslatableItem(
            configProvider.getOrLoad("menu_icons", "../global/"),
            "next-page",
            ThankmasPlugin.instance().globalTranslations
        )
    }

    private var currentIndex: Int = -1
    private var pages: MutableList<Menu> = mutableListOf(Menu(titleKey, size, menuFormat = menuFormat))

    init {
        createNewPage()
    }

    /** Opens this paginated menu to [player]. */
    public fun open(player: Player) {
        pages.first().open(player)
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
        val newPage = Menu(titleKey, size, menuFormat = menuFormat)

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
            pages[currentIndex - 1].setIcon(menuFormat.getSlotForChar('N'),
                Icon({ context, _ ->
                    val clicker = context.clicker

                    newPage.open(clicker)
                    clicker.playSound(Sound.BLOCK_WOODEN_BUTTON_CLICK_ON)
                }) {
                    NEXT_PAGE.buildItem(it.locale()) {
                        parsed("page", currentIndex + 1)
                    }
                })

            newPage.setIcon(menuFormat.getSlotForChar('P'),
                Icon({ context, _ ->
                    val clicker = context.clicker

                    pages[currentIndex - 1].open(clicker)
                    clicker.playSound(Sound.BLOCK_WOODEN_BUTTON_CLICK_ON)
                }) {
                    PREVIOUS_PAGE.buildItem(it.locale()) {
                        parsed("page", currentIndex)
                    }
                })
        } else {
            newPage.setIcon(menuFormat.getSlotForChar('P'), Icon({ context, _ ->
                val clicker = context.clicker

                clicker.closeInventory()
                clicker.playSound(Sound.BLOCK_WOODEN_BUTTON_CLICK_ON)
            }) { EXIT.buildItem(it.locale()) })
        }
    }


    /** @returns the last page. */
    private fun lastPage(): Menu {
        return pages[currentIndex]
    }

}