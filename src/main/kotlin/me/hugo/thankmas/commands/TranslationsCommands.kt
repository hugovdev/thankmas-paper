package me.hugo.thankmas.commands

import me.hugo.thankmas.ThankmasPlugin
import me.hugo.thankmas.lang.Translated
import me.hugo.thankmas.player.PaperPlayerData
import me.hugo.thankmas.player.PlayerDataManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Optional
import revxrsal.commands.bukkit.annotation.CommandPermission

public class TranslationsCommands<T : PaperPlayerData>(
    private val playerManager: PlayerDataManager<T>,
) : Translated {

    private enum class TranslationType {
        GLOBAL, LOCAL
    }

    @Command("reloadtranslations")
    @CommandPermission("thankmas.admin")
    private fun reloadTranslations(sender: Player, @Optional type: TranslationType = TranslationType.LOCAL) {
        when (type) {
            TranslationType.LOCAL -> miniPhrase.translationRegistry.reload()
            TranslationType.GLOBAL -> ThankmasPlugin.instance().globalTranslations.translationRegistry.reload()
        }

        playerManager.getPlayerData(sender.uniqueId).setTranslation(sender.locale())

        sender.sendMessage(Component.text("Reloaded messages in context $type!", NamedTextColor.GREEN))
    }

}