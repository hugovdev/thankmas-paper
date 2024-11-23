package me.hugo.thankmas.cosmetics

import me.hugo.thankmas.SimpleThankmasPlugin
import me.hugo.thankmas.config.enumOrNull
import me.hugo.thankmas.items.TranslatableItem
import me.hugo.thankmas.lang.TranslatedComponent
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemFlag
import java.util.*

public class Cosmetic(
    public val id: String,
    public val slot: EquipmentSlot,
    public val color: Color?,
    public val price: Int,
    private val modelId: String = "cosmetics/$id",
) : TranslatedComponent {

    /** Reads a cosmetic from a configuration file. */
    public constructor(config: FileConfiguration, id: String) : this(
        id,
        config.enumOrNull<EquipmentSlot>("$id.slot") ?: EquipmentSlot.HEAD,
        config.getString("$id.color")?.let { Color.fromRGB(Integer.parseInt(it.removePrefix("#"), 16)) },
        config.getInt("$id.price"),
        config.getString("$id.model-id")?.let { "cosmetics/$it" } ?: "cosmetics/$id"
    )

    public val nameKey: String = "cosmetics.$id.item.name"

    public val item: TranslatableItem = TranslatableItem(
        material = Material.LEATHER_HORSE_ARMOR,
        name = nameKey,
        model = modelId,
        color = color?.asRGB() ?: -1,
        flags = listOf(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE, ItemFlag.HIDE_ADDITIONAL_TOOLTIP),
        miniPhrase = SimpleThankmasPlugin.instance().globalTranslations
    ).apply {
        editBaseItem {
            it.editMeta {
                val equippable = it.equippable
                equippable.slot = slot
                it.setEquippable(equippable)
            }
        }
    }

    public fun give(player: Player, locale: Locale = player.locale()) {
        player.inventory.setItem(slot, item.buildItem(locale))
    }

}