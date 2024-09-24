package me.hugo.thankmas.listener

import me.hugo.thankmas.ThankmasPlugin
import me.hugo.thankmas.lang.TranslatedComponent
import me.hugo.thankmas.player.PaperPlayerData
import me.hugo.thankmas.player.PlayerDataManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLocaleChangeEvent

/** Listens to player language changes. */
public class PlayerLocaleDetector<T : PaperPlayerData>(
    private val playerManager: PlayerDataManager<T>
) : Listener, TranslatedComponent {

    private val translations = ThankmasPlugin.instance().globalTranslations

    @EventHandler
    private fun onLocaleChange(event: PlayerLocaleChangeEvent) {
        val player = event.player
        val newLocale = event.locale()

        // Only run when the player has already logged in and locale
        // is actually changing!
        if (!player.isOnline || newLocale == player.locale()) return

        val playerData = playerManager.getPlayerData(player.uniqueId)
        playerData.setLocale(newLocale)
        player.sendMessage(translations.translate("general.locale_change", newLocale) {
            parsed("locale", newLocale.toLanguageTag())
        })
    }

}