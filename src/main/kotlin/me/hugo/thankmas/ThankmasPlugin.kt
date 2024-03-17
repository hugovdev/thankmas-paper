package me.hugo.thankmas

import dev.kezz.miniphrase.MiniPhrase
import dev.kezz.miniphrase.i18n.PropertiesFileTranslationRegistry
import me.hugo.thankmas.config.ConfigurationProvider
import me.hugo.thankmas.dependencyinjection.ThankmasModules
import me.hugo.thankmas.items.clickable.ClickableItemRegistry
import me.hugo.thankmas.listener.MenuManager
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module
import java.io.File
import java.util.*

/**
 * JavaPlugin that also registers default translations for
 * the plugin and registers dependency injection for these
 * APIs utility classes.
 */
public open class ThankmasPlugin : JavaPlugin(), KoinComponent {

    private val configProvider: ConfigurationProvider by inject()

    /** Global minimessage instance with all custom tags and styling. */
    public lateinit var miniMessage: MiniMessage.Builder

    /** Default plugin translations in: "plugins/plugin_name/lang" */
    public lateinit var translations: DefaultTranslations

    /** Global shared translations for all Thankmas plugins. */
    public lateinit var globalTranslations: MiniPhrase

    public companion object {
        private var instance: ThankmasPlugin? = null

        public fun instance(): ThankmasPlugin {
            val instance = instance
            requireNotNull(instance) { "Tried to fetch a ThankmasPlugin instance while it's null!" }

            return instance
        }
    }

    override fun onEnable() {
        instance = this

        // Register the dependency injection modules.
        startKoin { modules(ThankmasModules().module) }

        miniMessage = MiniMessage.builder().tags(TagResolver.builder().apply {
            val stylesConfig = configProvider.getOrLoad("custom_styles", "../global/")

            stylesConfig.getConfigurationSection("custom_styles")?.getKeys(false)?.forEach {
                val style = TextColor.fromHexString(stylesConfig.getString("custom_styles.$it", "#fffff")!!) ?: return@forEach

                tag(it, Tag.styling(style))
            }

            resolver(TagResolver.standard())
        }.build())

        translations = DefaultTranslations(File(dataFolder, "/lang/"), miniMessage)

        globalTranslations = MiniPhrase.configureAndBuild {
            miniMessage(translations.translations.miniMessage)
            translationRegistry(PropertiesFileTranslationRegistry(File(dataFolder, "../global/lang/"), "global_", true))
            defaultLocale(Locale.US)
        }

        val pluginManager = Bukkit.getPluginManager()

        val itemRegistry: ClickableItemRegistry by inject()
        pluginManager.registerEvents(itemRegistry, this)

        val menuManager: MenuManager by inject()
        pluginManager.registerEvents(menuManager, this)
    }

}