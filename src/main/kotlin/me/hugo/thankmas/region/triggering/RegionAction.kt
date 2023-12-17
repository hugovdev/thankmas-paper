package me.hugo.thankmas.region.triggering

import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent

public enum class RegionAction(public val action: ((player: Player) -> Unit)) : KoinComponent {



}