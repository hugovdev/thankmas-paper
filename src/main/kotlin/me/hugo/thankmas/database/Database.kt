package me.hugo.thankmas.database

import kotlinx.datetime.Instant
import org.bukkit.configuration.file.FileConfiguration
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.component.KoinComponent

/** SQL Table that saves the selected rod, hat and settings. */
public object PlayerData : Table("player_data") {
    public val uuid: Column<String> = varchar("uuid", 36)
    public val selectedRod: Column<String> = varchar("selected_rod", 30).default("")
    public val selectedCosmetic: Column<String> = varchar("selected_cosmetic", 30).default("")
    public val currency: Column<Int> = integer("currency_amount").default(0)

    override val primaryKey: PrimaryKey = PrimaryKey(uuid)
}

/** Table that saves all the fishes every player has caught. */
public object Fishes : Table("fish_caught") {
    public val whoCaught: Column<String> = varchar("uuid", 36)
    public val fishType: Column<String> = varchar("fish_type", 30)
    public val pondId: Column<String> = varchar("pond_id", 30)
    public val time: Column<Instant> = timestamp("time")

    override val primaryKey: PrimaryKey = PrimaryKey(whoCaught, time)
}

/** Table that saves all the cosmetics every player has. */
public object CosmeticsOwned : Table("cosmetics_owned") {
    public val whoOwns: Column<String> = varchar("uuid", 36)
    public val cosmeticId: Column<String> = varchar("cosmetic_id", 30)
    public val time: Column<Instant> = timestamp("time")

    override val primaryKey: PrimaryKey = PrimaryKey(whoOwns, cosmeticId)
}


/** Table that saves all the npcs every player has caught. */
public object FoundNPCs : Table("npcs_found") {
    public val whoFound: Column<String> = varchar("uuid", 36)
    public val npcId: Column<String> = varchar("npc_id", 30)
    public val time: Column<Instant> = timestamp("time")

    override val primaryKey: PrimaryKey = PrimaryKey(whoFound, npcId)
}

/** Table that saves all the fishing rods every player has unlocked and when. */
public object Rods : Table("unlocked_rods") {
    public val owner: Column<String> = varchar("uuid", 36)
    public val rodId: Column<String> = varchar("rod_id", 30)
    public val time: Column<Instant> = timestamp("time")

    override val primaryKey: PrimaryKey = PrimaryKey(owner, rodId)
}

/**
 * Creates the main tables for the lobby plugin and
 * provides a data source for connections.
 */
public class Database(config: FileConfiguration) : ConfigurableDatasource(config), KoinComponent {

    public val database: Database = Database.connect(dataSource)

    init {
        transaction {
            SchemaUtils.createMissingTablesAndColumns(PlayerData, Fishes, Rods, FoundNPCs, CosmeticsOwned)
        }
    }

}