package me.hugo.thankmas.commands

import me.hugo.thankmas.cosmetics.Cosmetic
import org.bukkit.entity.Player
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Optional
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.bukkit.annotation.CommandPermission

@Command("cosmetics")
public class CosmeticsCommand {

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