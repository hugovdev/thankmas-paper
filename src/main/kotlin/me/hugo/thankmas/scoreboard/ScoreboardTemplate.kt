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
public class ScoreboardTemplate<T : ScoreboardPlayerData<T>>(
    private val key: String,
    private val templateId: String,
    private val scoreboardManager: ScoreboardTemplateManager<T>
) : TranslatedComponent {

    /** Every line in the scoreboard for every language. */
    // lang -> [lines]
    private val boardLines: MutableMap<Locale, List<String>> = mutableMapOf()

    /** Which lines contain certain tag in each language. */
    // langKey -> [tag -> lines that contain the tag]
    private val tagLocations: MutableMap<Locale, MutableMap<String, List<Int>>> = mutableMapOf()
    private val usedResolvers: MutableMap<String, (player: Player, preferredLocale: Locale?) -> Tag> = mutableMapOf()

    /** Which tags are used in each line in each language. */
    // langKey [line -> tags]
    private val inversedTagLocations: MutableMap<Locale, MutableMap<Int, MutableList<String>>> = mutableMapOf()

    init {
        miniPhrase.translationRegistry.getLocales().forEach { language ->
            val lines = miniPhrase.translationRegistry.getList(key, language) ?: return@forEach
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
    public fun printBoard(player: Player, locale: Locale? = null) {
        val language = getValidLanguage(player, locale)

        val translatedResolvers = usedResolvers
            .map { tagData -> TagResolver.resolver(tagData.key, tagData.value.invoke(player, language)) }.toTypedArray()

        val lines = boardLines[language]!!
            .map { line ->
                if (line.isEmpty()) Component.empty() else miniPhrase.format(line) {
                    translatedResolvers.forEach { resolver(it) }
                }
            }

        val playerData = scoreboardManager.playerManager.getPlayerData(player.uniqueId)

        playerData.lastBoardId = templateId
        playerData.getBoard().updateLines(lines)
    }

    /**
     * @returns [preferredLocale] if specified and valid, if not, it checks for the player
     * locale, and if that one isn't valid, it uses the default one.
     */
    private fun getValidLanguage(player: Player, preferredLocale: Locale? = null): Locale {
        val playerLocale = player.locale()

        return if (preferredLocale != null) {
            if (!boardLines[preferredLocale].isNullOrEmpty()) preferredLocale
            else miniPhrase.defaultLocale
        } else if (boardLines[playerLocale].isNullOrEmpty()) miniPhrase.defaultLocale
        else playerLocale
    }

    /** @returns the locations of every tag in this language. */
    private fun getTagLocations(locale: Locale): MutableMap<String, List<Int>> {
        val locations = tagLocations[locale] ?: tagLocations[miniPhrase.defaultLocale]
        requireNotNull(locations) { "Tag locations for language ${locale.toLanguageTag()} could not be found." }

        return locations
    }

    /** Updates every line that contains any of the [tags] for [player]. */
    public fun updateLinesForTag(player: Player, vararg tags: String) {
        val locations = mutableListOf<Int>()
        val language = getValidLanguage(player)

        tags.forEach {
            getTagLocations(language)[it]?.let { newLocations -> locations.addAll(newLocations) }
        }

        val boardLines = boardLines[language]
        requireNotNull(boardLines) { "Board lines for $key are null for ${language.toLanguageTag()} and default lang!" }

        locations.toSet().forEach {
            val inversedTags = inversedTagLocations[language] ?: inversedTagLocations[miniPhrase.defaultLocale]
            requireNotNull(inversedTags) { "Could not find inversed scoreboard tag locations for ${language.toLanguageTag()}!" }

            scoreboardManager.playerManager.getPlayerData(player.uniqueId).getBoard()
                .updateLine(it, miniPhrase.format(boardLines[it]) {

                    inversedTags[it]?.forEach { tag ->
                        val resolver = usedResolvers[tag]
                        requireNotNull(resolver) { "Couldn't find resolver for scoreboard tag $tag!" }

                        resolver(TagResolver.resolver(tag, resolver(player, language)))
                    }
                })
        }
    }
}