package me.hugo.thankmas.region.types

import me.hugo.thankmas.markers.Marker
import me.hugo.thankmas.region.WeakRegion
import org.bukkit.entity.Player
import org.bukkit.util.Vector

/** Launches players with configurable speeds. */
public class JumpPadRegion(marker: Marker) : WeakRegion(marker) {

    private val vector: List<Double> = marker.getDoubleList("vector") ?: listOf(0.0, 0.0, 0.0)
    private val directionMultiplier: Double = marker.getDouble("directionMultipler") ?: 1.0

    override fun onEnter(player: Player) {
        player.velocity =
            player.location.direction.multiply(directionMultiplier).add(Vector(vector[0], vector[1], vector[2]))
    }

}