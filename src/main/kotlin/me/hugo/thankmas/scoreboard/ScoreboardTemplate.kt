package me.hugo.thankmas.scoreboard

import me.hugo.thankmas.lang.TranslatedComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.entity.Player
import org.koin.core.component.inject

/**
 * Representation of a scoreboard configured
 * in the language files.
 */
public class ScoreboardTemplate(private val key: String) : TranslatedComponent {

    private val scoreboardManager: ScoreboardTemplateManager by inject()

    /** Every line in the scoreboard for every language. */
    // lang -> [lines]
    private val boardLines: MutableMap<String, List<String>> = mutableMapOf()

    /** Which lines contain certain tag in each language. */
    // langKey -> [tag -> lines that contain the tag]
    private val tagLocations: MutableMap<String, MutableMap<String, List<Int>>> = mutableMapOf()
    private val usedResolvers: MutableMap<String, (player: Player) -> String> = mutableMapOf()

    /** Which tags are used in each line in each language. */
    // langKey [line -> tags]
    private val inversedTagLocations: MutableMap<String, MutableMap<Int, MutableList<String>>> = mutableMapOf()

    init {
        miniPhrase.translationRegistry.getLocales().forEach { language ->
            val lines = miniPhrase.translationRegistry.getTranslationList(key, language)
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
        val language = player.locale().language ?: miniPhrase.defaultLocale.language

        val translatedResolvers = usedResolvers
            .map { tagData -> Placeholder.parsed(tagData.key, tagData.value.invoke(player)) }.toTypedArray()

        val lines = boardLines[language]!!
            .map { line ->
                if (line.isEmpty()) Component.empty() else miniPhrase.format(line) {
                    translatedResolvers.forEach { resolver(it) }
                }
            }

        // playerData.fastBoard?.updateLines(lines)
    }

    /** Updates every line that contains any of the [tags] for [player]. */
    public fun updateLinesForTag(player: Player, vararg tags: String) {
        val locations = mutableListOf<Int>()
        val language = player.locale().language ?: miniPhrase.defaultLocale.language

        tags.forEach {
            tagLocations[language]!![it]?.let { newLocations -> locations.addAll(newLocations) }
        }

        val boardLines = boardLines[language]!!

        locations.toSet().forEach {
            /*playerData.fastBoard?.updateLine(it, miniPhrase.format(boardLines[it]) {
                inversedTagLocations[language]!![it]?.forEach { tag ->
                    parsed(tag, usedResolvers[tag]?.invoke(player) ?: tag)
                }
            }
            )*/
        }
    }
}