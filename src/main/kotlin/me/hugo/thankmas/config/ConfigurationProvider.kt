package me.hugo.thankmas.config

import me.hugo.thankmas.util.HashBiMap
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.koin.core.annotation.Single
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * Provides an easy way to load and cache configuration files.
 */
@Single
public class ConfigurationProvider {

    /** Cached configuration files. */
    private val configs = HashBiMap<String, FileConfiguration>()

    /** File that belongs to each config name. */
    private val files: MutableMap<String, File> = mutableMapOf()

    /** @returns the file that contains this config. */
    public fun getFile(config: String): File? {
        return files[config]
    }

    /** @returns the config name by the config object. */
    public fun getNameFromConfig(config: FileConfiguration): String? {
        return configs.inverse[config]
    }

    /** Gets a cached configuration file or loads and caches it. */
    public fun getOrLoad(path: String): FileConfiguration {
        return configs.computeIfAbsent(path) { load(path) }
    }

    /** Gets a cached configuration file or loads and caches it. */
    public fun getOrResources(file: String, path: String = ""): FileConfiguration {
        return configs.computeIfAbsent(file) { loadOrResources(file, path) }
    }

    /** Reloads a configuration file and saves in cache the new version. */
    public fun reload(config: String): FileConfiguration {
        val reloadedConfig = load(config)
        configs[config] = reloadedConfig

        return reloadedConfig
    }

    /** Loads a configuration file. */
    private fun load(path: String): FileConfiguration {
        val configFile = files.computeIfAbsent(path) {
            File(Bukkit.getPluginsFolder(), path)
        }

        return YamlConfiguration.loadConfiguration(configFile)
    }

    /** Loads a configuration file. */
    private fun loadOrResources(file: String, path: String = ""): FileConfiguration {
        val configFile = files.computeIfAbsent(file) {
            File(Bukkit.getPluginsFolder(), "$path/$file")
        }

        if (!configFile.exists()) {
            configFile.parentFile.mkdirs()

            configFile.createNewFile()
            val resourceStream = javaClass.getResourceAsStream("/$file")

            if (resourceStream != null) {
                Files.copy(
                    resourceStream,
                    configFile.getAbsoluteFile().toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                )
            }
        }

        return YamlConfiguration.loadConfiguration(configFile)
    }

}