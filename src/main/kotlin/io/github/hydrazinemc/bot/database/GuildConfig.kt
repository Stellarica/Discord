package io.github.hydrazinemc.bot.database

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update


object GuildConfigTable: LongIdTable() {
	val guild = varchar("guild", 256) // The guild
	val punishmentLogChannel = varchar("punishmentLogChannel", 256) // The punishment log channel ID
	val botLogChannel = varchar("botLogChannel", 256) // The bot log channel ID
}

fun ensureHasConfig(guildId: Snowflake) {
	transaction {
		if ((GuildConfigTable.select(GuildConfigTable.guild eq guildId.value.toString()).fetchSize ?: 0) == 0) {
			GuildConfigTable.insert { row ->
				row[guild] = guildId.value.toString()
				row[punishmentLogChannel] = ""
				row[botLogChannel] = ""
			}
		}
	}
}

var GuildBehavior.punishmentLogChannel: Snowflake?
	get() = Snowflake(value=getGuildConfig(this.id, GuildConfigTable.punishmentLogChannel) ?: "")
	set(value) = setGuildConfig(this.id, GuildConfigTable.punishmentLogChannel, value?.value.toString())

var GuildBehavior.botLogChannel: Snowflake?
	get() = Snowflake(value=getGuildConfig(this.id, GuildConfigTable.botLogChannel) ?: "")
	set(value) = setGuildConfig(this.id, GuildConfigTable.botLogChannel, value?.value.toString())


fun setGuildConfig(guild: Snowflake, column: Column<String>, value: String) {
	ensureHasConfig(guild)
	transaction {
		GuildConfigTable.update({
			GuildConfigTable.guild eq guild.value.toString()
		}) { it[column] = value }
	}
}

fun getGuildConfig(guild: Snowflake, column: Column<String>): String? {
	return transaction {
		GuildConfigTable.select {
			GuildConfigTable.guild eq guild.value.toString()
		}.map { it[column] }.first()
	}
}