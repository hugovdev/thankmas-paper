package me.hugo.thankmas

import me.hugo.thankmas.di.ThankmasModules
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module
import java.io.File

/**
 * JavaPlugin that also registers default translations for
 * the plugin and registers dependency injection for these
 * APIs utility classes.
 */
public open class ThankmasPlugin : JavaPlugin() {

    /** Default plugin translations in: "plugins/plugin_name/lang" */
    protected val translations: DefaultTranslations = DefaultTranslations(File(dataFolder, "/lang/"))

    override fun onEnable() {
        // Register the dependency injection modules.
        startKoin { modules(ThankmasModules().module) }
    }

}