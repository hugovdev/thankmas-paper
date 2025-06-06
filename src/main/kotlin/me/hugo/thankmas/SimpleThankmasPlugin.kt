package me.hugo.thankmas

import dev.kezz.miniphrase.MiniPhrase
import dev.kezz.miniphrase.i18n.PropertiesFileTranslationRegistry
import me.hugo.thankmas.config.ConfigurationProvider
import me.hugo.thankmas.dependencyinjection.ThankmasModules
import me.hugo.thankmas.git.GitHubHelper
import me.hugo.thankmas.items.clickable.ClickableItemRegistry
import me.hugo.thankmas.listener.InfiniteProjectiles
import me.hugo.thankmas.listener.MenuManager
import me.hugo.thankmas.listener.PrivateUseAreaCharacterBlocker
import me.hugo.thankmas.world.s3.S3WorldSynchronizer
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.plus
import org.koin.ksp.generated.module
import java.io.File
import java.util.*

/**
 * JavaPlugin that also registers default translations for
 * the plugin and registers dependency injection for these
 * APIs utility classes.
 */
public abstract class SimpleThankmasPlugin(
    public val configScopes: List<String> = listOf(),
    public val localTranslationDirectory: String =
        if (configScopes.isNotEmpty()) "${configScopes.first()}/lang"
        else "local",
    private val koinModuleProvider: () -> List<Module>
) : JavaPlugin(), KoinComponent {

    private val gitHubHelper: GitHubHelper by inject()

    protected val configProvider: ConfigurationProvider by inject()
    protected val s3WorldSynchronizer: S3WorldSynchronizer by inject()

    /** Whether to download the global scope of configuration files! */
    protected open val downloadGlobalScope: Boolean = true

    /** Global minimessage instance with all custom tags and styling. */
    public lateinit var miniMessage: MiniMessage.Builder

    /** Default plugin translations in: "plugins/plugin_name/lang" */
    public lateinit var translations: DefaultTranslations

    /** Global shared translations for all Thankmas plugins. */
    public lateinit var globalTranslations: MiniPhrase

    public companion object {
        private var simpleInstance: SimpleThankmasPlugin? = null

        public fun instance(): SimpleThankmasPlugin {
            return requireNotNull(simpleInstance)
            { "Tried to fetch a ThankmasPlugin instance while it's null!" }
        }
    }

    override fun onLoad() {
        simpleInstance = this

        // Register the dependency injection modules.
        startKoin {
            modules(koinModuleProvider().plus(ThankmasModules().module))
        }

        downloadConfigFiles()
    }

    override fun onEnable() {
        miniMessage = MiniMessage.builder().tags(TagResolver.builder().apply {
            val stylesConfig = configProvider.getOrLoad("global/custom_styles.yml")

            stylesConfig.getConfigurationSection("custom_styles")?.getKeys(false)?.forEach {
                val style =
                    TextColor.fromHexString(stylesConfig.getString("custom_styles.$it", "#fffff")!!) ?: return@forEach

                tag(it, Tag.styling(style))
            }

            resolver(TagResolver.standard())
        }.build())

        translations = DefaultTranslations(File(Bukkit.getPluginsFolder(), "$localTranslationDirectory/"), miniMessage)

        globalTranslations = MiniPhrase.configureAndBuild {
            miniMessage(translations.translations.miniMessage)
            translationRegistry(PropertiesFileTranslationRegistry(File(Bukkit.getPluginsFolder(), "global/lang/")))
            defaultLocale(Locale.US)
        }

        val pluginManager = Bukkit.getPluginManager()

        val itemRegistry: ClickableItemRegistry by inject()
        pluginManager.registerEvents(itemRegistry, this)

        val menuManager: MenuManager by inject()
        pluginManager.registerEvents(menuManager, this)
        pluginManager.registerEvents(InfiniteProjectiles(), this)
        pluginManager.registerEvents(PrivateUseAreaCharacterBlocker(), this)
    }

    /** Downloads all the config files from GitHub. */
    private fun downloadConfigFiles() {
        logger.info("Starting scope download...")

        // Download all the global configurations.
        if (downloadGlobalScope) gitHubHelper.downloadScope("global")

        // Download the local configurations for the current scope.
        configScopes.forEach { gitHubHelper.downloadScope(it) }
    }
}