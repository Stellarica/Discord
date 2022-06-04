package io.github.hydrazinemc.bot.database

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import io.github.hydrazinemc.bot.getSnowflake
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update


object GuildConfigTable : LongIdTable() {
	val guild = varchar("guild", 256) // The guild
	val punishmentLogChannel = varchar("punishmentLogChannel", 256) // The punishment log channel ID
	val botLogChannel = varchar("botLogChannel", 256) // The bot log channel ID
}

private fun ensureHasConfig(guildId: Snowflake) {
	if ((GuildConfigTable.select(GuildConfigTable.guild eq guildId.value.toString()).fetchSize ?: 0) == 0) {
		GuildConfigTable.insert { row ->
			row[guild] = guildId.value.toString()
			row[punishmentLogChannel] = ""
			row[botLogChannel] = ""
		}
	}
}

private fun setGuildConfig(guild: Snowflake, column: Column<String>, value: String) {
	ensureHasConfig(guild)
	GuildConfigTable.update({
		GuildConfigTable.guild eq guild.value.toString()
	}) { it[column] = value }
}

private fun getGuildConfig(guild: Snowflake, column: Column<String>): String? {
	return transaction {
		GuildConfigTable.select {
			GuildConfigTable.guild eq guild.value.toString()
		}.map { it[column] }.firstOrNull()
	}
}

// note that is has to be assigned back to the guild
data class GuildConfig(
	val guild: Snowflake,
	var punishmentLogChannel: Snowflake?,
	var botLogChannel: Snowflake?
)

var GuildBehavior.config: GuildConfig
	get() = transaction {
		GuildConfig(
			this@config.id,
			getSnowflake(getGuildConfig(this@config.id, GuildConfigTable.punishmentLogChannel)),
			getSnowflake(getGuildConfig(this@config.id, GuildConfigTable.botLogChannel))
		)
	}
	set(value) {
		transaction {
			setGuildConfig(
				this@config.id,
				GuildConfigTable.punishmentLogChannel,
				value.punishmentLogChannel?.value.toString()
			)
			setGuildConfig(this@config.id, GuildConfigTable.botLogChannel, value.botLogChannel?.value.toString())
		}
	}
