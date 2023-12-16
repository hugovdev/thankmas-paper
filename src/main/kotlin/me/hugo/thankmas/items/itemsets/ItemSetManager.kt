package me.hugo.thankmas.items.itemsets

import me.hugo.thankmas.items.ClickableItem
import me.hugo.thankmas.items.getKeyedData
import me.hugo.thankmas.items.hasKeyedData
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.persistence.PersistentDataType
import org.koin.core.annotation.Single

/**
 * Loads sets of clickable items from config and registers
 * items to make them clickable through listeners.
 */
@Single
public class ItemSetManager() : Listener {

    // ClickableItem id -> ClickableItem
    private val clickableItems: MutableMap<String, ClickableItem> = mutableMapOf()
    private val itemSets: MutableMap<String, List<ClickableItem>> = mutableMapOf()

    /** Loads every item set in [config]. Section: "item-sets". */
    public fun initialize(config: FileConfiguration) {
        config.getConfigurationSection("item-sets")?.getKeys(false)?.forEach { setId ->
            config.getConfigurationSection("item-sets.$setId")?.getKeys(false)
                ?.map {
                    ClickableItem("$setId/$it", config, "item-sets.$setId.$it")
                        .also { item -> registerClickableItem(item) }
                }?.let {
                    itemSets[setId] = it
                }
        }
    }

    /** Registers [item] to make it clickable. */
    public fun registerClickableItem(item: ClickableItem) {
        clickableItems[item.id] = item
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

    @EventHandler
    private fun onItemClick(event: PlayerInteractEvent) {
        if (!event.action.isRightClick) return
        val item = event.item ?: return

        val clickableItemId =
            item.getKeyedData(ClickableItem.CLICKABLE_ITEM_ID, PersistentDataType.STRING) ?: return
        val clickableItem = clickableItems[clickableItemId] ?: return

        event.player.chat("/${clickableItem.command}")

        event.isCancelled = true
    }

    @EventHandler
    private fun onItemSwap(event: PlayerSwapHandItemsEvent) {
        if (event.mainHandItem.hasKeyedData(ClickableItem.CLICKABLE_ITEM_ID, PersistentDataType.STRING) ||
            event.offHandItem.hasKeyedData(ClickableItem.CLICKABLE_ITEM_ID, PersistentDataType.STRING)
        ) {
            event.isCancelled = true
            return
        }
    }

}