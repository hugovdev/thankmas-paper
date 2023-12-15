package me.hugo.thankmas.scoreboard

import me.hugo.thankmas.lang.TranslatedComponent
import me.hugo.thankmas.player.ScoreboardPlayerData
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.entity.Player
import java.util.*

/**
 * Representation of a scoreboard configured
 * in the language files.
 */
public class ScoreboardTemplate<T : ScoreboardPlayerData>(
    private val key: String,
    private val scoreboardManager: ScoreboardTemplateManager<T>
) : TranslatedComponent {

    /** Every line in the scoreboard for every language. */
    // lang -> [lines]
    private val boardLines: MutableMap<Locale, List<String>> = mutableMapOf()

    /** Which lines contain certain tag in each language. */
    // langKey -> [tag -> lines that contain the tag]
    private val tagLocations: MutableMap<Locale, MutableMap<String, List<Int>>> = mutableMapOf()
    private val usedResolvers: MutableMap<String, (player: Player) -> Tag> = mutableMapOf()

    /** Which tags are used in each line in each language. */
    // langKey [line -> tags]
    private val inversedTagLocations: MutableMap<Locale, MutableMap<Int, MutableList<String>>> = mutableMapOf()

    init {
        miniPhrase.translationRegistry.getLocales().forEach { language ->
            val lines = miniPhrase.translationRegistry.getList(key, language)
            boardLines[language] = lines

            scoreboardManager.tagResolvers.forEach { (tag, resolver) ->
                val locations = mutableListOf<Int>()

                lines.forEachIndexed { index, line ->
                    if (line.contains("<$tag>")) locations.add(index)
                    inversedTagLocations.computeIfAbsent(language) { mutableMapOf() }
                        .computeIfAbsent(index) { mutableListOf() }.add(tag)
                }

                if (locations.isNotEmpty()) {
                    tagLocations.computeIfAbsent(language) { mutableMapOf() }[tag] = locations
                    usedResolvers[tag] = resolver
                }
            }
        }
    }

    /** Prints this scoreboard to [player]. */
    public fun printBoard(player: Player) {
        val language = player.locale()

        val translatedResolvers = usedResolvers
            .map { tagData -> TagResolver.resolver(tagData.key, tagData.value.invoke(player)) }.toTypedArray()

        val lines = boardLines[language]!!
            .map { line ->
                if (line.isEmpty()) Component.empty() else miniPhrase.format(line) {
                    translatedResolvers.forEach { resolver(it) }
                }
            }

        scoreboardManager.playerManager.getPlayerData(player.uniqueId).getBoard().updateLines(lines)
    }

    /** Updates every line that contains any of the [tags] for [player]. */
    public fun updateLinesForTag(player: Player, vararg tags: String) {
        val locations = mutableListOf<Int>()
        val language = player.locale()

        tags.forEach {
            tagLocations[language]!![it]?.let { newLocations -> locations.addAll(newLocations) }
        }

        val boardLines = boardLines[language]!!

        locations.toSet().forEach {
            scoreboardManager.playerManager.getPlayerData(player.uniqueId).getBoard()
                .updateLine(it, miniPhrase.format(boardLines[it]) {
                    inversedTagLocations[language]!![it]?.forEach { tag ->
                        TagResolver.resolver(
                            tag,
                            usedResolvers[tag]?.invoke(player) ?: Tag.inserting { Component.text(tag) })
                    }
                })
        }
    }
}