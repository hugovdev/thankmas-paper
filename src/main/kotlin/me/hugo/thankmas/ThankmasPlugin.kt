package me.hugo.thankmas

import me.hugo.thankmas.config.string
import me.hugo.thankmas.database.Database
import me.hugo.thankmas.database.PlayerPropertyManager
import me.hugo.thankmas.player.PlayerBasics
import me.hugo.thankmas.player.PlayerDataManager
import me.hugo.thankmas.player.ScoreboardPlayerData
import me.hugo.thankmas.player.cosmetics.PlayerWardrobe
import me.hugo.thankmas.scoreboard.ScoreboardTemplateManager
import me.hugo.thankmas.world.registry.AnvilWorldRegistry
import org.bukkit.Bukkit
import org.bukkit.World
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.module.Module

/**
 * JavaPlugin that also registers default translations for
 * the plugin and registers dependency injection for these
 * APIs utility classes.
 */
public abstract class ThankmasPlugin<T : ScoreboardPlayerData<T>>(
    configScopes: List<String> = listOf(),
    koinModuleProvider: () -> List<Module> = { emptyList() },
    localTranslationDirectory: String = if (configScopes.isNotEmpty()) "${configScopes.first()}/lang" else "local",
) : SimpleThankmasPlugin(configScopes, localTranslationDirectory, koinModuleProvider), KoinComponent {

    public abstract val playerDataManager: PlayerDataManager<T>
    public abstract val scoreboardTemplateManager: ScoreboardTemplateManager<T>

    private lateinit var databaseConnector: Database
    protected val playerPropertyManager: PlayerPropertyManager by inject()

    protected val anvilWorldRegistry: AnvilWorldRegistry by inject()

    public companion object {
        private var instance: ThankmasPlugin<*>? = null

        public fun <P: ThankmasPlugin<*>> instance(): P {
            return requireNotNull(instance as? P)
            { "Tried to fetch a ThankmasPlugin instance while it's null!" }
        }
    }

    /** Main world's name for this server. */
    public open val worldNameOrNull: String? = null

    /** Returns the main world name, assuring it's not null. */
    public val worldName: String
        get() = requireNotNull(worldNameOrNull)
        { "Tried to get the world name for the main world, but its null!" }

    /** Main world accessor. */
    public val world: World
        get() = requireNotNull(worldNameOrNull?.let { Bukkit.getWorld(it) }) { "Tried to use the main world before it was ready." }

    /** Whether to try and fetch the main world for S3 on start-up. */
    protected open val downloadWorld: Boolean = true

    override fun onLoad() {
        super.onLoad()
        instance = this

        logger.info("* Creating Database connector...")
        databaseConnector = Database(configProvider.getOrLoad("global/database.yml"))
        logger.info("* Created correctly!")

        logger.info("* Initializing player properties...")
        initializeProperties()
        logger.info("* Initialized!")

        logger.info("* Initializing and loading the main world...")
        loadMainWorld()
        logger.info("* Done!")
    }

    override fun onDisable() {
        super.onDisable()

        logger.info("* Saving all player data...")

        // Save all player data before disabling!
        this.playerDataManager.getAllPlayerData().forEach {
            it.forceSave(it.onlinePlayer)
        }

        logger.info("* Saved!")

        databaseConnector.dataSource.close()
    }

    /** Initializes properties by creating the necessary tables and assigning accessors. */
    protected open fun initializeProperties() {
        // Basic player info like selected cosmetics, fishing rod and their currency.
        playerPropertyManager.initialize(
            "player_basics",
            { PlayerBasics() },
            PlayerBasics.serializer()
        )

        // Collection of unlocked cosmetics.
        playerPropertyManager.initialize(
            "player_wardrobe",
            { PlayerWardrobe() },
            PlayerWardrobe.serializer()
        )
    }

    /** Downloads and loads all markers for the main world. */
    protected open fun loadMainWorld() {
        val worldToUse = worldNameOrNull ?: return

        if (downloadWorld) {
            val scopeWorld = configProvider.getOrLoad("${configScopes.first()}/config.yml").string("main-world")
            logger.info("Downloading it from scope $scopeWorld...")

            Bukkit.unloadWorld(worldToUse, false)

            s3WorldSynchronizer.downloadWorld(
                scopeWorld,
                Bukkit.getWorldContainer().resolve(worldToUse).also { it.mkdirs() })
        }

        anvilWorldRegistry.loadMarkers(worldToUse)
    }
}