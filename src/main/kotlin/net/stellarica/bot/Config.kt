package net.stellarica.bot

import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable

@Serializable
data class Config(
	val testerChannel: MutableMap<Snowflake, Snowflake> = mutableMapOf()
)