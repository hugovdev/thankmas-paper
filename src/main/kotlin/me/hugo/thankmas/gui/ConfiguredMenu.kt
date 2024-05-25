package me.hugo.thankmas.gui

import com.noxcrew.interfaces.interfaces.ChestInterface
import com.noxcrew.interfaces.interfaces.ChestInterfaceBuilder
import dev.kezz.miniphrase.MiniPhraseContext
import me.hugo.thankmas.player.translate
import org.bukkit.configuration.file.FileConfiguration

/** Menu that fetches its properties from a config file and path. */
public open class ConfiguredMenu(config: FileConfiguration, path: String) : Menu(
    config.getString("$path.title") ?: "$path.title",
    config.getInt("$path.size", 9 * 3),
    menuFormat = config.getString("$path.format")?.uppercase()?.let { MenuFormat.valueOf(it) }
)

/** Creates a new [ChestInterface] using a [ChestInterfaceBuilder]. */
context(MiniPhraseContext)
public inline fun buildConfiguredChestInterface(
    config: FileConfiguration,
    path: String,
    builder: ChestInterfaceBuilder.() -> Unit
): ChestInterface =
    ChestInterfaceBuilder().also {
        it.rows = config.getInt("$path.rows", 3)
        val title = config.getString("$path.title") ?: "$path.title"

        it.withTransform { _, view ->
            view.title(view.player.translate(title))
        }
    }.also(builder).build()