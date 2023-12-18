package me.hugo.thankmas.items.itemsets

import me.hugo.thankmas.items.clickable.ClickableItem
import me.hugo.thankmas.items.clickable.ClickableItemRegistry
import me.hugo.thankmas.registry.MapBasedRegistry
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Locale

/**
 * Loads sets of clickable items from config and registers
 * them in the [ClickableItemRegistry].
 *
 * ItemSets are a list of clickable items that can be given in certain
 * situations, for example, when joining a lobby, arena or area.
 */
@Single
public class ItemSetRegistry(config: FileConfiguration) : MapBasedRegistry<String, List<ClickableItem>>(),
    KoinComponent {

    private val itemRegistry: ClickableItemRegistry by inject()

    init {
        config.getConfigurationSection("item-sets")?.getKeys(false)?.forEach { setId ->
            config.getConfigurationSection("item-sets.$setId")?.getKeys(false)
                ?.map {
                    ClickableItem("$setId/$it", config, "item-sets.$setId.$it")
                        .also { item -> itemRegistry.register(item.id, item) }
                }?.let {
                    register(setId, it)
                }
        }
    }

    /** Gives every item in the set with id [id] to [player]. */
    public fun giveSet(id: String, player: Player, locale: Locale? = null) {
        get(id).forEach { it.give(player, locale) }
    }

}