package me.hugo.thankmas.registry

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * Provides getters and register functions for any kind
 * of registry.
 */
public open class MapBasedRegistry<K, V> {

    /** Map containing every register. */
    private val registry: ConcurrentMap<K, V> = ConcurrentHashMap()

    /** Registers [value] on [key]. */
    public fun register(key: K, value: V, replace: Boolean = true) {
        if (registry.containsKey(key) && !replace) return

        registry[key] = value
    }

    /** @returns the value for this key, can be null. */
    public fun getOrNull(key: K): V? {
        return registry[key]
    }

    /** @returns the value for this key. */
    public fun get(key: K): V {
        val value = getOrNull(key)
        requireNotNull(value) { "Tried to get value for key \"$key\" but it doesn't exist!" }

        return value
    }

    /** @returns all the registered keys. */
    public fun getKeys(): Collection<K> {
        return registry.keys
    }

    /** @returns all the registered values. */
    public fun getValues(): Collection<V> {
        return registry.values
    }

    /** @returns the amount of registers this registry has. */
    public fun size(): Int {
        return registry.size
    }

}