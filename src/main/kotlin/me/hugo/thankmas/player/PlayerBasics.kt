package me.hugo.thankmas.player

import kotlinx.serialization.Serializable

/** Contains basic player information like their currency and selected rod and cosmetics. */
@Serializable
public data class PlayerBasics(
    public var currency: Int = 0,
    public var selectedRod: String = "",
    public var selectedCosmetic: String = ""
)