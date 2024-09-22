package me.hugo.thankmas.gui.paginated

import me.hugo.thankmas.config.enum
import me.hugo.thankmas.config.string
import me.hugo.thankmas.gui.Menu
import me.hugo.thankmas.items.TranslatableItem
import org.bukkit.configuration.file.FileConfiguration

public class ConfigurablePaginatedMenu(config: FileConfiguration, path: String, lastMenu: Menu? = null) : PaginatedMenu(
    config.string("$path.title"),
    config.getInt("$path.size", 9 * 3),
    config.enum<Menu.MenuFormat>("$path.format"),
    config.getConfigurationSection("$path.icon")?.let { TranslatableItem(config, "$path.icon") },
    lastMenu
)