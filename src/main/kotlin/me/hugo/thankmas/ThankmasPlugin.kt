package me.hugo.thankmas

import me.hugo.thankmas.database.CosmeticsOwned
import me.hugo.thankmas.database.Database
import me.hugo.thankmas.database.PlayerData
import me.hugo.thankmas.player.PlayerDataManager
import me.hugo.thankmas.player.ScoreboardPlayerData
import me.hugo.thankmas.scoreboard.ScoreboardTemplateManager
import org.jetbrains.exposed.sql.Table
import org.koin.core.component.KoinComponent

/**
 * JavaPlugin that also registers default translations for
 * the plugin and registers dependency injection for these
 * APIs utility classes.
 */
public abstract class ThankmasPlugin<T : ScoreboardPlayerData<T>>(
    configScopes: List<String> = listOf(),
    localTranslationDirectory: String = if (configScopes.isNotEmpty()) "${configScopes.first()}/lang" else "local",
    private val sqlTables: Array<Table> = arrayOf(PlayerData, CosmeticsOwned)
) : SimpleThankmasPlugin(configScopes, localTranslationDirectory, true), KoinComponent {

    public abstract val playerDataManager: PlayerDataManager<T>
    public abstract val scoreboardTemplateManager: ScoreboardTemplateManager<T>

    protected lateinit var databaseConnector: Database

    public companion object {
        private var instance: ThankmasPlugin<*>? = null

        public fun instance(): ThankmasPlugin<*> {
            return requireNotNull(instance)
            { "Tried to fetch a ThankmasPlugin instance while it's null!" }
        }
    }

    override fun onLoad() {
        super.onLoad()
        instance = this
    }

    override fun onEnable() {
        super.onEnable()

        logger.info("Creating Database connector and tables...")
        databaseConnector = Database(configProvider.getOrLoad("global/database.yml"), *sqlTables)
        logger.info("Connected and created correctly!")
    }

    override fun onDisable() {
        super.onDisable()

        logger.info("Saving all player data...")

        // Save all player data before disabling!
        this.playerDataManager.getAllPlayerData().forEach {
            it.forceSave(it.onlinePlayer)
        }

        logger.info("Saved!")

        databaseConnector.dataSource.close()
    }
}