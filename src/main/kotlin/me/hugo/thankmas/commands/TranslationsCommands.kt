package me.hugo.thankmas.commands

import me.hugo.thankmas.ThankmasPlugin
import me.hugo.thankmas.config.ConfigurationProvider
import me.hugo.thankmas.git.GitHubHelper
import me.hugo.thankmas.lang.TranslatedComponent
import me.hugo.thankmas.player.PaperPlayerData
import me.hugo.thankmas.player.PlayerDataManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.tag.Tag
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.koin.core.component.inject
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Optional
import revxrsal.commands.bukkit.annotation.CommandPermission

public class TranslationsCommands<P : PaperPlayerData<P>>(
    private val playerManager: PlayerDataManager<P>,
) : TranslatedComponent {

    private enum class TranslationType {
        GLOBAL, LOCAL
    }

    private val instance = ThankmasPlugin.instance()
    private val configProvider: ConfigurationProvider by inject()
    private val gitHubHelper: GitHubHelper by inject()

    @Command("reloadtranslations")
    @CommandPermission("thankmas.admin")
    private fun reloadTranslations(sender: Player, @Optional type: TranslationType = TranslationType.LOCAL) {
        sender.sendMessage(Component.text("Fetching translations from context $type...", NamedTextColor.GREEN))

        object : BukkitRunnable() {
            override fun run() {
                when (type) {
                    TranslationType.LOCAL -> {
                        val localTranslationDirectory = instance.localTranslationDirectory

                        gitHubHelper.downloadScope(localTranslationDirectory)

                        miniPhrase.translationRegistry.reload()

                    }

                    TranslationType.GLOBAL -> {
                        gitHubHelper.downloadScope("global/lang")

                        instance.globalTranslations.translationRegistry.reload()
                    }
                }

                object : BukkitRunnable() {
                    override fun run() {
                        playerManager.getPlayerData(sender.uniqueId).setLocale(sender.locale())
                        sender.sendMessage(Component.text("Reloaded messages in context $type!", NamedTextColor.GREEN))
                    }
                }.runTask(instance)
            }
        }.runTaskAsynchronously(instance)
    }

    @Command("previewmessage")
    @CommandPermission("thankmas.admin")
    private fun previewTranslation(
        sender: Player,
        key: String,
        @Optional type: TranslationType = TranslationType.LOCAL
    ) {
        val instance = ThankmasPlugin.instance()

        when (type) {
            TranslationType.LOCAL -> sender.sendMessage(
                instance.translations.translations.translate(
                    key,
                    sender.locale()
                )
            )

            TranslationType.GLOBAL -> sender.sendMessage(instance.globalTranslations.translate(key, sender.locale()))
        }
    }

    @Command("reloadstyles")
    @CommandPermission("thankmas.admin")
    private fun reloadStyles(sender: Player) {
        val instance = ThankmasPlugin.instance()

        instance.miniMessage = instance.miniMessage.editTags { tagResolver ->
            val stylesConfig = configProvider.reload("global/custom_styles.yml")

            stylesConfig.getConfigurationSection("custom_styles")?.getKeys(false)?.forEach {
                val style =
                    TextColor.fromHexString(stylesConfig.getString("custom_styles.$it", "#fffff")!!) ?: return@forEach

                tagResolver.tag(it, Tag.styling(style))
            }
        }

        val newMiniMessage = instance.miniMessage.build()

        instance.globalTranslations.miniMessage = newMiniMessage
        instance.translations.translations.miniPhrase.miniMessage = newMiniMessage

        sender.sendMessage(Component.text("Reloaded message styles!", NamedTextColor.GREEN))
    }

}