package me.hugo.thankmas.scoreboard

import me.hugo.thankmas.lang.Translated
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.Tag
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Registry of every [ScoreboardTemplate] used in the plugin.
 */
public interface ScoreboardTemplateManager : Translated {

    /** Templates that have been loaded and are ready to be used. */
    public val loadedTemplates: MutableMap<String, ScoreboardTemplate>

    /** Player-specific tag suppliers. */
    public val tagResolvers: MutableMap<String, (player: Player) -> Tag>

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
    private fun loadTemplates() {}

    /**
     * Loads from translations and caches every tag location,
     * resolver and translation.
     */
    private fun loadTemplate(key: String) {
        loadedTemplates[key] = ScoreboardTemplate(key)
    }

    /**
     * Registers every tag usable in scoreboards and
     * what they should return.
     */
    private fun registerTags() {
        registerTag("date") {
            Tag.selfClosingInserting {
                Component.text(
                    DateTimeFormatter.ofPattern("MM/dd/yyyy").format(LocalDateTime.now())
                )
            }
        }

        registerTag("players") { Tag.selfClosingInserting { Component.text(Bukkit.getOnlinePlayers().size) } }
    }

    /** Registers [tag] which returns the result of running [resolver]. */
    private fun registerTag(tag: String, resolver: (player: Player) -> Tag) {
        tagResolvers[tag] = resolver
    }
}