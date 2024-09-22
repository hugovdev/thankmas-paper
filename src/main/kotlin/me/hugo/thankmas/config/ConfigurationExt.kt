package me.hugo.thankmas.config

import org.bukkit.configuration.file.FileConfiguration

public fun FileConfiguration.string(path: String): String {
    val string = this.getString(path)
    requireNotNull(string) { "Configuration field at $path is required but missing!" }

    return string
}

public inline fun <reified T : Enum<T>> FileConfiguration.enum(path: String): T {
    val string = this.getString(path)
    requireNotNull(string) { "Configuration field at $path is required but missing!" }

    return enumValueOf<T>(string.uppercase())
}
