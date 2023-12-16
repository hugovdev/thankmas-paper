package me.hugo.thankmas.gui

import org.bukkit.configuration.file.FileConfiguration

/** Menu that fetches its properties from a config file and path. */
public open class ConfiguredMenu(config: FileConfiguration, path: String) : Menu(
    config.getString("$path.title", "$path.menu-title")!!,
    config.getInt("$path.size", 9 * 3),
    menuFormat = MenuFormat.valueOf(config.getString("$path.menu-title", "one_row")?.uppercase()!!)
)