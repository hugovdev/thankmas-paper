package me.hugo.thankmas.listener

import io.papermc.paper.event.player.AsyncChatEvent
import me.hugo.thankmas.ThankmasPlugin
import me.hugo.thankmas.lang.Translated
import me.hugo.thankmas.player.PlayerDataManager
import me.hugo.thankmas.player.rank.RankedPlayerData
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

/**
 * Registers default rank chat format and basic message filtering.
 */
public class RankedPlayerChat<T : RankedPlayerData<T>>(
    private val playerManager: PlayerDataManager<T>,
    private val shouldSee: (viewer: Player, sender: Player) -> Boolean
) : Listener, Translated {

    // Only allow normal symbols when chatting! (Avoid negative space symbols or glyphs)
    private val normalChatRegex = "^[a-zA-Z0-9_\\-. %<>!@?:()*+',]*\$".toRegex()
    private val translations = ThankmasPlugin.instance().globalTranslations

    @EventHandler
    private fun onPlayerChat(event: AsyncChatEvent) {
        val text = (event.message() as TextComponent).content()

        val chatter = event.player

        if (!normalChatRegex.matches(text)) {
            chatter.sendMessage(translations.translate("general.chat.invalid", chatter.locale()))

            event.isCancelled = true
            return
        }

        event.viewers().removeIf { it is Player && !shouldSee(it, chatter) }

        event.renderer { source, _, message, viewer ->
            if (viewer is Player) {
                val sourceData = playerManager.getPlayerData(source.uniqueId)

                translations.translate(
                    "rank.${sourceData.getPrimaryGroupName(source)}.chat",
                    viewer.locale()
                ) {
                    inserting("message", message)
                    parsed("player", source.name)
                    inserting("nametag", sourceData.getNameTag())
                }
            } else Component.text(("${source.name}: ")).append(message)
        }
    }

}