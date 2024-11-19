package me.hugo.thankmas.listener

import io.papermc.paper.event.player.AsyncChatEvent
import me.hugo.thankmas.ThankmasPlugin
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

/** Bans the use of Private Use Area chars in char, user input and output. */
public class PrivateUseAreaCharacterBlocker : Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    private fun onPlayerChat(event: AsyncChatEvent) {
        val text = PlainTextComponentSerializer.plainText().serialize(event.message())
        val chatter = event.player

        // If the chat message contains Private Use Area symbols, don't let the message go through!
        // https://jrgraphix.net/r/Unicode/E000-F8FF
        if (text.any { Character.UnicodeBlock.of(it) == Character.UnicodeBlock.PRIVATE_USE_AREA }) {
            chatter.sendMessage(
                ThankmasPlugin.instance().globalTranslations.translate(
                    "general.chat.invalid",
                    chatter.locale()
                )
            )

            event.isCancelled = true
            return
        }
    }

}