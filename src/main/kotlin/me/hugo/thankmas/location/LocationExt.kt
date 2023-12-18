package me.hugo.thankmas.location

import org.bukkit.Bukkit
import org.bukkit.Location

public fun Location.serializeString(): String {
    return "$world , $x , $y , $z , $pitch , $yaw"
}

public fun String.deserializeLocation(): Location {
    val split = split(" , ")

    require(split.size == 6) { "Location \"$this\" doesn't follow the correct format." }

    return Location(
        Bukkit.getWorld(split[0]), split[1].toDouble(), split[2].toDouble(), split[3].toDouble(),
        split[4].toFloat(), split[5].toFloat()
    )
}