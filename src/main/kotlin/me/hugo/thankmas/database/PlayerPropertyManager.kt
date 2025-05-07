package me.hugo.thankmas.database

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.koin.core.annotation.Single
import java.util.*
import kotlin.reflect.KClass

/** A table where properties are saved. */
public class PlayerPropertyTable(propertyName: String) : Table(propertyName) {
    public val uuid: Column<UUID> = uuid("uuid").index()
    public val value: Column<String> = text("value")

    override val primaryKey: PrimaryKey = PrimaryKey(uuid)
}

/** Saves the table being used for this property, the default value and the serializer to use. */
public data class PlayerProperty<T : Any>(
    private val tableName: String,
    private val defaultValueProvider: (uuid: UUID) -> T,
    private val serializer: KSerializer<T>
) {

    /** Create the table for this serialized table. */
    private val table: PlayerPropertyTable = PlayerPropertyTable(tableName)

    /** Initializes the table used by this property. */
    public fun initializeTable(): Unit = SchemaUtils.createMissingTablesAndColumns(table)

    /** Gets the data saved for the key [uuid] */
    public fun get(uuid: UUID): T = getOr(uuid, defaultValueProvider(uuid))

    /** Gets the data saved for the key [uuid] and returns [default] if not present. */
    public fun getOr(uuid: UUID, default: T): T {
        return table.selectAll().where { table.uuid eq uuid }.firstOrNull()?.get(table.value)
            ?.let { Json.decodeFromString(serializer, it) } ?: default
    }

    /** Sets [value] for key [uuid]. */
    public fun write(uuid: UUID, value: T) {
        table.upsert {
            it[table.uuid] = uuid
            it[table.value] = Json.encodeToString(serializer, value)
        }
    }
}

/** Saves every player property accessible. */
@Single
public class PlayerPropertyManager {

    public val properties: MutableMap<KClass<*>, PlayerProperty<*>> = mutableMapOf()

    public inline fun <reified T : Any> initialize(
        tableName: String,
        noinline defaultValueProvider: (uuid: UUID) -> T,
        serializer: KSerializer<T>
    ): PlayerProperty<T> {
        return PlayerProperty(tableName, defaultValueProvider, serializer).also {
            properties[T::class] = it
            it.initializeTable()
        }
    }

    public inline fun <reified T : Any> getPropertyOrNull(): PlayerProperty<T>? =
        properties[T::class] as? PlayerProperty<T>

    public inline fun <reified T : Any> getProperty(): PlayerProperty<T> = requireNotNull(getPropertyOrNull())
    { "Tried to get player property storage data for ${T::class.simpleName} but it doesn't exist!" }
}