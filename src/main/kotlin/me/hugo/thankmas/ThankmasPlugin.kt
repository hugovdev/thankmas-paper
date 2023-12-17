package me.hugo.thankmas

import me.hugo.thankmas.di.ThankmasModules
import me.hugo.thankmas.items.clickable.ClickableItemRegistry
import me.hugo.thankmas.listener.MenuListener
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module
import java.io.File

/**
 * JavaPlugin that also registers default translations for
 * the plugin and registers dependency injection for these
 * APIs utility classes.
 */
public open class ThankmasPlugin : JavaPlugin(), KoinComponent {

    /** Default plugin translations in: "plugins/plugin_name/lang" */
    protected val translations: DefaultTranslations = DefaultTranslations(File(dataFolder, "/lang/"))
    private val itemRegistry: ClickableItemRegistry by inject()

    override fun onEnable() {
        // Register the dependency injection modules.
        startKoin { modules(ThankmasModules().module) }

        val pluginManager = Bukkit.getPluginManager()

        pluginManager.registerEvents(itemRegistry, this)
        pluginManager.registerEvents(MenuListener(), this)
    }

}