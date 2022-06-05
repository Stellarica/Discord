package io.github.hydrazinemc.bot

import com.kotlindiscord.kord.extensions.DISCORD_GREEN
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.rest.builder.message.EmbedBuilder
import io.github.hydrazinemc.bot.extensions.config.config

/*
 * avoids this:
 * java.lang.NumberFormatException: Invalid number format: 'null'
	at kotlin.text.StringsKt__StringNumberConversionsKt.numberFormatError(StringNumberConversions.kt:203)
	at kotlin.text.UStringsKt.toULong(UStrings.kt:109)
	at dev.kord.common.entity.Snowflake.<init>(Snowflake.kt:49)
 */
fun getSnowflake(id: String?): Snowflake? {
	id ?: return null
	try {
		return Snowflake(id)
	} catch (e: NumberFormatException) {
		return null
	}
}

fun getSnowflake(id: Long?) = id?.let { Snowflake(it) }

suspend fun GuildBehavior.sendModerationEmbed(embed: EmbedBuilder.() -> Unit) {
	val log = this.config.punishmentLogChannel
	if (log != null) {
		this.kord.getChannelOf<MessageChannel>(log)?.createEmbed(embed)
	}
}

suspend fun GuildBehavior.sendLogEmbed(embed: EmbedBuilder.() -> Unit) {
	val log = this.config.botLogChannel
	if (log != null) {
		this.kord.getChannelOf<MessageChannel>(log)?.createEmbed(embed)
	}
}
