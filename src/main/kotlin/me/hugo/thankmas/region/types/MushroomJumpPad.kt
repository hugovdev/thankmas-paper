package me.hugo.thankmas.region.types

import me.hugo.thankmas.markers.Marker
import me.hugo.thankmas.region.WeakRegion
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import org.bukkit.util.Vector

/** Launches players with configurable speeds. */
public class MushroomJumpPad(marker: Marker) : WeakRegion(marker) {

    private val vector: List<Double> = marker.getDoubleList("vector") ?: listOf(0.0, 0.0, 0.0)
    private val directionMultiplier: Double = marker.getDouble("directionMultipler") ?: 1.0

    // Only shoot the player when they jump out of the jump-pad region!
    override fun onLeave(player: Player) {
        if (player.velocity.y <= 0) return

        val playerLocation = player.location

        player.velocity =
            playerLocation.direction.multiply(directionMultiplier).add(Vector(vector[0], vector[1], vector[2]))

        playerLocation.world.playSound(
            playerLocation,
            "minecraft:save_the_kweebecs.mushroom_boing",
            SoundCategory.AMBIENT, 1.0f, 1.0f
        )
    }

}