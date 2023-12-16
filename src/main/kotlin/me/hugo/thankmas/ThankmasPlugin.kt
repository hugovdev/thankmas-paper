package me.hugo.thankmas

import me.hugo.thankmas.di.ThankmasModules
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module
import java.io.File

public open class ThankmasPlugin : JavaPlugin() {

    protected val translations: DefaultTranslations = DefaultTranslations(File(dataFolder, "/lang/"))

    override fun onEnable() {
        super.onEnable()
        startKoin { modules(ThankmasModules().module) }
    }

}