package me.hugo.thankmas.gui.paginated

import me.hugo.thankmas.gui.Menu
import me.hugo.thankmas.items.TranslatableItem
import org.bukkit.configuration.file.FileConfiguration

public class ConfigurablePaginatedMenu(config: FileConfiguration, path: String) : PaginatedMenu(
    config.getString("$path.title") ?: "$path.title",
    config.getInt("$path.size", 9 * 3),
    menuFormat = (config.getString("$path.format") ?: "one_row").uppercase().let { Menu.MenuFormat.valueOf(it) },
    config.getConfigurationSection("$path.icon")?.let { TranslatableItem(config, "$path.icon") }
)