package me.hugo.thankmas.commands

import me.hugo.thankmas.entity.npc.PlayerNPCMarkerRegistry
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("thankmas debug")
@CommandPermission("thankmas.admin")
public class NPCCommands: KoinComponent {

    @Subcommand("generate-dynamic-skins")
    private fun generateSkins(sender: Player) {
        val playerNPCMarkerRegistry: PlayerNPCMarkerRegistry by inject()

        playerNPCMarkerRegistry.generateSkins()
    }

}