package me.hugo.thankmas.scoreboard

import me.hugo.thankmas.lang.Translated
import me.hugo.thankmas.player.PlayerDataManager
import me.hugo.thankmas.player.ScoreboardPlayerData
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.Tag
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Registry of every [ScoreboardTemplate] used in the plugin.
 * Needs a player registry to get a player's scoreboard.
 */
public open class ScoreboardTemplateManager<T : ScoreboardPlayerData>(
    public val playerManager: PlayerDataManager<T>
) : Translated {

    /** Templates that have been loaded and are ready to be used. */
    private val loadedTemplates: MutableMap<String, ScoreboardTemplate<T>> = mutableMapOf()

    /** Player-specific tag suppliers. */
    public val tagResolvers: MutableMap<String, (player: Player, preferredLocale: Locale?) -> Tag> = mutableMapOf()

    /** Registers the tags and loads the templates. */
    public fun initialize() {
        registerTags()
        loadTemplates()
    }

    /**
     * Loads the scoreboard templates that will be used
     * in this Thankmas Plugin!
     *
     * Should run after registering the tags.
     */
    protected open fun loadTemplates() {}

    /**
     * Loads from translations and caches every tag location,
     * resolver and translation.
     */
    protected fun loadTemplate(key: String, customKey: String = key) {
        loadedTemplates[customKey] = ScoreboardTemplate(key, this)
    }

    /** @returns the scoreboard template for this key, can be null.  */
    public fun getTemplateOrNull(key: String): ScoreboardTemplate<T>? {
        return loadedTemplates[key]
    }

    /** @returns the scoreboard template for this key.  */
    public fun getTemplate(key: String): ScoreboardTemplate<T> {
        val template = getTemplateOrNull(key)
        requireNotNull(template) { "Tried to fetch a null scoreboard template." }

        return template
    }

    /**
     * Registers every tag usable in scoreboards and
     * what they should return.
     */
    protected open fun registerTags() {
        registerTag("date") { _, _ ->
            Tag.selfClosingInserting {
                Component.text(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy").format(LocalDateTime.now())
                )
            }
        }

        registerTag("players") { _, _ -> Tag.selfClosingInserting { Component.text(Bukkit.getOnlinePlayers().size) } }
    }

    /** Registers [tag] which returns the result of running [resolver]. */
    protected fun registerTag(tag: String, resolver: (player: Player, preferredLocale: Locale?) -> Tag) {
        tagResolvers[tag] = resolver
    }
}