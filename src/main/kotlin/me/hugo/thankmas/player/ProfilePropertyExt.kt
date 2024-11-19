package me.hugo.thankmas.player

import com.destroystokyo.paper.profile.ProfileProperty
import me.hugo.thankmas.config.string
import org.bukkit.configuration.file.FileConfiguration

public fun skinProperty(config: FileConfiguration, path: String): ProfileProperty =
    skinProperty(config.string("$path.texture"), config.string("$path.signature"))

public fun skinProperty(value: String, signature: String): ProfileProperty =
    ProfileProperty("textures", value, signature)