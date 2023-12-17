package me.hugo.thankmas.config

import me.hugo.thankmas.ThankmasPlugin
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.koin.core.annotation.Single
import java.io.File

/**
 * Provides an easy way to load and cache configuration files.
 */
@Single
public class ConfigurationProvider {

    private val configs: MutableMap<String, FileConfiguration> = mutableMapOf()

    /** Gets a cached configuration file or loads and caches it. */
    public fun getOrLoad(config: String): FileConfiguration {
        return configs.computeIfAbsent(config) { load(config) }
    }

    /** Reloads a configuration file and saves in cache the new version. */
    public fun reload(config: String): FileConfiguration {
        val reloadedConfig = load(config)
        configs[config] = reloadedConfig

        return reloadedConfig
    }

    /** Loads a configuration file. */
    public fun load(config: String): FileConfiguration {
        val fileName = "$config.yml"
        val configFile = File(ThankmasPlugin.instance().dataFolder, fileName)

        if (!configFile.exists()) {
            configFile.parentFile.mkdirs()
            ThankmasPlugin.instance().saveResource(fileName, false)
        }

        return YamlConfiguration.loadConfiguration(configFile)
    }

}