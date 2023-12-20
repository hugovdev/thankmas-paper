package me.hugo.thankmas.database

import org.bukkit.configuration.file.FileConfiguration

/**
 * Datasource connector that fetches the data source
 * properties from a configuration file.
 */
public open class ConfigurableDatasource(config: FileConfiguration, path: String? = null) : DatasourceConnector(
    config.getString(path?.let { "$it.ip" } ?: "ip", "")!!,
    config.getString(path?.let { "$it.port" } ?: "port", "3306")!!,
    config.getString(path?.let { "$it.schema" } ?: "schema", "thankmas_data")!!,
    config.getString(path?.let { "$it.user" } ?: ".user", "user")!!,
    config.getString(path?.let { "$it.password" } ?: "password", "")!!
)