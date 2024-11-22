package me.hugo.thankmas.gui

import com.noxcrew.interfaces.interfaces.ChestInterface
import com.noxcrew.interfaces.interfaces.ChestInterfaceBuilder
import dev.kezz.miniphrase.MiniPhraseContext
import me.hugo.thankmas.player.translate
import org.bukkit.configuration.file.FileConfiguration

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