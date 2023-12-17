package me.hugo.thankmas.database

import org.bukkit.configuration.file.FileConfiguration

/**
 * Datasource connector that fetches the data source
 * properties from a configuration file.
 */
public class ConfigurableDatasource(config: FileConfiguration, path: String) : DatasourceConnector(
    config.getString("$path.ip", "")!!,
    config.getString("$path.port", "3306")!!,
    config.getString("$path.schema", "thankmas_data")!!,
    config.getString("$path.user", "user")!!,
    config.getString("$path.password", "")!!
)