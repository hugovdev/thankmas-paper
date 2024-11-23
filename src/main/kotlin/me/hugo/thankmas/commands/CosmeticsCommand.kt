package me.hugo.thankmas.commands

import me.hugo.thankmas.ThankmasPlugin
import me.hugo.thankmas.cosmetics.Cosmetic
import me.hugo.thankmas.cosmetics.CosmeticsRegistry
import me.hugo.thankmas.player.cosmetics.CosmeticsPlayerData
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.DefaultFor
import revxrsal.commands.annotation.Optional
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("cosmetics")
public class CosmeticsCommand : KoinComponent {

    @DefaultFor("cosmetics")
    private fun openCosmeticsMenu(sender: Player) {
        val cosmeticsRegistry: CosmeticsRegistry by inject()

        val playerData =
            ThankmasPlugin.instance().playerDataManager.getPlayerData(sender.uniqueId) as? CosmeticsPlayerData ?: return

        if (!playerData.doesUpdateCosmetic(sender)) return

        cosmeticsRegistry.openSelector(sender)
    }

    @Subcommand("try")
    @CommandPermission("thankmas.admin")
    private fun give(
        sender: Player,
        cosmetic: Cosmetic,
        @Optional receiver: Player = sender,
    ) {
        cosmetic.give(sender)
    }

}