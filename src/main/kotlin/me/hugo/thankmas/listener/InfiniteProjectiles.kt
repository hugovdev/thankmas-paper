package me.hugo.thankmas.listener

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent
import me.hugo.thankmas.items.hasKeyedData
import me.hugo.thankmas.listener.InfiniteProjectiles.Companion.INFINITE_PROJECTILE_KEY
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

/**
 * Sets projectiles to consume = false when they have
 * the [INFINITE_PROJECTILE_KEY] data present.
 */
public class InfiniteProjectiles : Listener {

    public companion object {
        public const val INFINITE_PROJECTILE_KEY: String = "infinite_projectiles"
    }

    @EventHandler
    public fun onProjectileLaunch(event: PlayerLaunchProjectileEvent) {
        event.setShouldConsume(!event.itemStack.hasKeyedData(INFINITE_PROJECTILE_KEY))
    }
}