package me.hugo.thankmas.player

import dev.kezz.miniphrase.MiniPhrase
import me.hugo.thankmas.ThankmasPlugin
import me.hugo.thankmas.entity.Hologram
import me.hugo.thankmas.region.Region
import me.hugo.thankmas.region.triggering.TriggeringRegion
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * PlayerData class that provides player
 * properties to get the bukkit player object.
 */
public open class PaperPlayerData<P : PlayerData<P>>(playerUUID: UUID, playerDataManager: PlayerDataManager<P>) :
    PlayerData<P>(playerUUID, playerDataManager) {

    protected val globalTranslations: MiniPhrase
        get() = ThankmasPlugin.instance().globalTranslations

    /** @returns the player object from the UUID if online, can be null. */
    public val onlinePlayerOrNull: Player?
        get() = Bukkit.getPlayer(playerUUID)?.takeIf { it.isOnline }

    /** @returns the player object from UUID if online. */
    public val onlinePlayer: Player
        get() {
            val player = onlinePlayerOrNull
            requireNotNull(player) { "Tried to access onlinePlayer while the player is null or offline." }

            return player
        }

    /** List of regions the player is in. */
    private val regions: MutableList<Region> = mutableListOf()

    /** Map of hologram entity ids that this players can see. */
    private val spawnedHolograms: ConcurrentMap<Hologram<*>, TextDisplay> = ConcurrentHashMap()

    /** @returns the entity id for this hologram. */
    public fun getDisplayForHologramOrNull(hologram: Hologram<*>): TextDisplay? {
        return spawnedHolograms[hologram]
    }

    /** Adds this hologram and entity ids to the [spawnedHolograms] map. */
    public fun addHologram(hologram: Hologram<*>, display: TextDisplay) {
        spawnedHolograms[hologram] = display
    }

    /** Removes [hologram] from the [spawnedHolograms] map. */
    public fun removeHologram(hologram: Hologram<*>) {
        spawnedHolograms.remove(hologram)
    }

    /** Removes every text display and hologram for this player. */
    public fun removeAllHolograms() {
        val iterator = spawnedHolograms.iterator()

        while (iterator.hasNext()) {
            iterator.next().value.remove()
            iterator.remove()
        }
    }

    /** Update every spawn hologram for [player]. */
    public fun updateHolograms(locale: Locale? = null) {
        spawnedHolograms.keys.forEach { it.spawnOrUpdate(onlinePlayer, locale) }
    }

    /** Runs when a player enters [region] or actively is inside. */
    public fun updateOnRegion(region: Region) {
        val player = onlinePlayer

        val triggeringRegion = region as? TriggeringRegion?

        if (regions.contains(region)) triggeringRegion?.onIdle?.invoke(player)
        else {
            triggeringRegion?.onEnter?.invoke(player)
            regions.add(region)
        }
    }

    /** Runs when a player leaves [region]. */
    public fun leaveRegion(region: Region) {
        val triggeringRegion = region as? TriggeringRegion?

        if (regions.remove(region)) {
            triggeringRegion?.onLeave?.invoke(onlinePlayer)
        }
    }

    /** Runs whenever the player changes translations. */
    public open fun setLocale(newLocale: Locale) {
        // Update holograms!
        updateHolograms(newLocale)
    }

    /** Saves player data safely in databases, asynchronously. */
    public open fun saveSafely(player: Player, onSuccess: () -> Unit) {
        val instance = ThankmasPlugin.instance()
        val startTime = System.currentTimeMillis()

        Bukkit.getScheduler().runTaskAsynchronously(instance, Runnable {
            save()

            Bukkit.getScheduler().runTask(instance, Runnable {
                onSave(player)
                onSuccess()
                instance.logger.info("Player info for $playerUUID saved and cleaned in ${System.currentTimeMillis() - startTime}ms.")
            })
        })
    }

    /** Saves player data! */
    protected open fun save() {}

    /** Runs after saving player data! */
    protected open fun onSave(player: Player) {
        removeAllHolograms()
    }

    /** Runs after the player profile has been loaded. */
    public open fun onPrepared(player: Player) {}

    /** Forces save without safely switching to an asynchronous thread. */
    public fun forceSave(player: Player) {
        save()
        onSave(player)
    }
}