package me.hugo.thankmas.cosmetics

import me.hugo.thankmas.registry.AutoCompletableMapRegistry
import org.bukkit.configuration.file.FileConfiguration
import org.koin.core.annotation.Single

/** Registry of player cosmetics! */
@Single
public class CosmeticsRegistry(config: FileConfiguration) : AutoCompletableMapRegistry<Cosmetic>(Cosmetic::class.java) {

    init {
        config.getKeys(false).forEach { register(it, Cosmetic(config, it)) }
    }

}