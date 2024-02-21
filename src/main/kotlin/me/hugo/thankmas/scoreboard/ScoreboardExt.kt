package me.hugo.thankmas.scoreboard

import net.kyori.adventure.text.Component
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.scoreboard.Team

/** @returns this scoreboard's team with name [name] if exists, if not it creates it. */
public fun Scoreboard.getOrCreateTeam(name: String): Team {
    return this.getTeam(name) ?: this.registerNewTeam(name)
}

/** @returns this scoreboard's objective with name [name] if exists, if not it creates it. */
public fun Scoreboard.getOrCreateObjective(
    name: String,
    criteria: Criteria,
    component: Component?,
    onCreate: (objective: Objective) -> Unit,
    onFinish: (objective: Objective) -> Unit
): Objective {
    return (this.getObjective(name) ?: this.registerNewObjective(name, criteria, component).also(onCreate)).also(onFinish)
}