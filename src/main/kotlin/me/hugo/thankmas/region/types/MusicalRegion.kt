package me.hugo.thankmas.region.types

import me.hugo.thankmas.markers.Marker
import me.hugo.thankmas.region.WorldRegion
import org.bukkit.World

/** Contains a song that should be played when the player is within this region. */
public class MusicalRegion(marker: Marker, regionId: String, world: World) : WorldRegion(marker, regionId, world) {

    public val songId: String = requireNotNull(marker.getString("songId"))

}