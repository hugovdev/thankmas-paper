package me.hugo.thankmas

import dev.kezz.miniphrase.MiniPhrase
import dev.kezz.miniphrase.i18n.PropertiesFileTranslationRegistry
import me.hugo.thankmas.dependencyinjection.ThankmasModules
import me.hugo.thankmas.items.clickable.ClickableItemRegistry
import me.hugo.thankmas.listener.MenuManager
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

    /** Default plugin translations in: "plugins/plugin_name/lang" */
    public val translations: DefaultTranslations = DefaultTranslations(File(dataFolder, "/lang/"))

    public val globalTranslations: MiniPhrase = MiniPhrase.configureAndBuild {
        miniMessage(translations.translations.miniMessage)
        translationRegistry(PropertiesFileTranslationRegistry(File(dataFolder, "../global/lang/"), "global_", true))
        defaultLocale(Locale.US)
    }

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

        val pluginManager = Bukkit.getPluginManager()

        val itemRegistry: ClickableItemRegistry by inject()
        pluginManager.registerEvents(itemRegistry, this)

        val menuManager: MenuManager by inject()
        pluginManager.registerEvents(menuManager, this)
    }

}