package me.hugo.thankmas.items.itemsets

import me.hugo.thankmas.items.clickable.ClickableItem
import me.hugo.thankmas.items.clickable.ClickableItemRegistry
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Loads sets of clickable items from config and registers
 * them in the [ClickableItemRegistry].
 */
@Single
public class ItemSetRegistry : KoinComponent {

    private val itemSets: MutableMap<String, List<ClickableItem>> = mutableMapOf()
    private val itemRegistry: ClickableItemRegistry by inject()

    /** Loads every item set in [config]. Section: "item-sets". */
    public fun initialize(config: FileConfiguration) {
        config.getConfigurationSection("item-sets")?.getKeys(false)?.forEach { setId ->
            config.getConfigurationSection("item-sets.$setId")?.getKeys(false)
                ?.map {
                    ClickableItem("$setId/$it", config, "item-sets.$setId.$it")
                        .also { item -> itemRegistry.registerItem(item) }
                }?.let {
                    itemSets[setId] = it
                }
        }
    }

    /** @returns the item set with id [id], can be null. */
    public fun getSetOrNull(id: String?): List<ClickableItem>? {
        return itemSets[id]
    }

    /** @returns the item set with id [id]. */
    public fun getSet(id: String?): List<ClickableItem> {
        val itemSet = getSetOrNull(id)
        requireNotNull(itemSet) { "Tried to fetch item set with id $id, but returned null." }

        return itemSet
    }

    /** Gives every item in the set with id [id] to [player]. */
    public fun giveSet(id: String, player: Player) {
        getSet(id).forEach { it.give(player) }
    }

}