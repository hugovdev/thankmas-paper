package me.hugo.thankmas.world

import com.google.common.collect.HashMultimap
import me.hugo.thankmas.markers.Marker
import me.hugo.thankmas.registry.MapBasedRegistry

/** World registry with loadable markers. */
public abstract class WorldRegistry<W> : MapBasedRegistry<String, W>() {

    /** Markers loaded for each world on this registry. */
    private val loadedMarkers: MutableMap<String, HashMultimap<String, Marker>> = mutableMapOf()

    /** Returns every marker for [key]. */
    public fun getMarkers(key: String): HashMultimap<String, Marker> =
        loadedMarkers.computeIfAbsent(key) { HashMultimap.create() }

    /** Returns every registered marker of [markerId] type for [key]. */
    public fun getMarkerForType(key: String = "world", markerId: String): Set<Marker> = getMarkers(key).get(markerId)

    /** Saves marker [marker] on the world [key]. */
    protected fun saveMarker(key: String, marker: Marker) {
        getMarkers(key).put(marker.getMarkerId(), marker)
    }

    /** Get the amount of loaded markers in [key]. */
    protected fun getMarkerCount(key: String): Int = loadedMarkers[key]?.size() ?: 0

    /** Returns the world if loaded, if not it will load it and load its markers. */
    public abstract fun getOrLoadWithMarkers(key: String): W

}