package me.hugo.thankmas

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializer
import me.hugo.thankmas.database.CosmeticsOwned
import me.hugo.thankmas.database.Database
import me.hugo.thankmas.database.PlayerData
import me.hugo.thankmas.database.PlayerPropertyManager
import me.hugo.thankmas.player.PlayerBasics
import me.hugo.thankmas.player.PlayerDataManager
import me.hugo.thankmas.player.ScoreboardPlayerData
import me.hugo.thankmas.player.cosmetics.PlayerWardrobe
import me.hugo.thankmas.scoreboard.ScoreboardTemplateManager
import me.hugo.thankmas.world.registry.AnvilWorldRegistry
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.Table
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * JavaPlugin that also registers default translations for
 * the plugin and registers dependency injection for these
 * APIs utility classes.
 */
public abstract class ThankmasPlugin<T : ScoreboardPlayerData<T>>(
    configScopes: List<String> = listOf(),
    localTranslationDirectory: String = if (configScopes.isNotEmpty()) "${configScopes.first()}/lang" else "local"
) : SimpleThankmasPlugin(configScopes, localTranslationDirectory, true), KoinComponent {

    public abstract val playerDataManager: PlayerDataManager<T>
    public abstract val scoreboardTemplateManager: ScoreboardTemplateManager<T>

    private lateinit var databaseConnector: Database
    protected val playerPropertyManager: PlayerPropertyManager by inject()

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

        logger.info("Creating Database connector...")
        databaseConnector = Database(configProvider.getOrLoad("global/database.yml"))
        logger.info("Created correctly!")

        playerPropertyManager.initialize(
            "player_basics",
            { PlayerBasics() },
            PlayerBasics.serializer()
        )

        playerPropertyManager.initialize(
            "player_wardrobe",
            { PlayerWardrobe() },
            PlayerWardrobe.serializer()
        )
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