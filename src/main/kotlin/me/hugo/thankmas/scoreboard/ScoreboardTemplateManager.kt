package me.hugo.thankmas.scoreboard

import me.hugo.thankmas.lang.TranslatedComponent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.koin.core.annotation.Single
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Registry of every [ScoreboardTemplate] used in the plugin.
 */
@Single
public open class ScoreboardTemplateManager : TranslatedComponent {

    private val loadedTemplates: MutableMap<String, ScoreboardTemplate> = mutableMapOf()
    public val tagResolvers: MutableMap<String, (player: Player) -> String> = mutableMapOf()

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
    protected fun loadTemplate(key: String) {
        loadedTemplates[key] = ScoreboardTemplate(key)
    }

    /**
     * Registers every tag usable in scoreboards and
     * what they should return.
     */
    protected open fun registerTags() {
        registerTag("date") { DateTimeFormatter.ofPattern("MM/dd/yyyy").format(LocalDateTime.now()) }
        registerTag("players") { Bukkit.getOnlinePlayers().size.toString() }
    }

    /** Registers [tag] which returns the result of running [resolver]. */
    private fun registerTag(tag: String, resolver: (player: Player) -> String) {
        tagResolvers[tag] = resolver
    }
}