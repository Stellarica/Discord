package io.github.hydrazinemc.bot

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.rest.builder.message.EmbedBuilder
import kotlinx.serialization.Serializable
import org.litote.kmongo.eq


suspend fun GuildBehavior.sendModerationEmbed(embed: EmbedBuilder.() -> Unit) {
	val log = this.getConfig().punishmentLogChannel
	if (log != null) {
		this.kord.getChannelOf<MessageChannel>(log)?.createEmbed(embed)
	}
}

suspend fun GuildBehavior.sendLogEmbed(embed: EmbedBuilder.() -> Unit) {
	val log = this.getConfig().botLogChannel
	if (log != null) {
		this.kord.getChannelOf<MessageChannel>(log)?.createEmbed(embed)
	}
}

@Serializable
data class GuildConfig(
	val guild: Snowflake,
	var punishmentLogChannel: Snowflake?,
	var botLogChannel: Snowflake?
)

suspend fun GuildBehavior.getConfig() =
	configCollection.findOne(GuildConfig::guild eq this@getConfig.id) ?: GuildConfig(this.id, null, null)

suspend fun GuildBehavior.setConfig(value: GuildConfig) =
	configCollection.findOneAndReplace(GuildConfig::guild eq this@setConfig.id, value)

