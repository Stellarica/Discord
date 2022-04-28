package io.github.hydrazinemc.bot.database

import dev.kord.common.entity.Snowflake
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update


object GuildConfigTable: LongIdTable() {
	val guild = varchar("guild", 256) // The guild
	val modrole = varchar("modrole", 256) // The moderation role ID
	val adminrole = varchar("adminrole", 256) // The admin role ID
	val punishmentLogChannel = varchar("punishmentLogChannel", 256) // The punishment log channel ID
	val botLogChannel = varchar("botLogChannel", 256) // The bot log channel ID
}

fun ensureHasConfig(guild: Snowflake) {
	transaction {
		if ((GuildConfigTable.select(GuildConfigTable.guild eq guild.value.toString()).fetchSize ?: 0) == 0) {
			GuildConfigTable.insert { row ->
				row[GuildConfigTable.guild] = guild.value.toString()
				row[GuildConfigTable.modrole] = ""
				row[GuildConfigTable.adminrole] = ""
				row[GuildConfigTable.punishmentLogChannel] = ""
				row[GuildConfigTable.botLogChannel] = ""
			}
		}
	}
}

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