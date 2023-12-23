package me.hugo.thankmas.leaderboard

import com.destroystokyo.paper.profile.ProfileProperty
import java.util.UUID

public data class LeaderboardEntry<T>(
    private val uuid: UUID,
    private val playerName: String,
    private val skin: ProfileProperty,
    private val value: T
)