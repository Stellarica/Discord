package io.github.hydrazinemc.bot.database

import com.kotlindiscord.kord.extensions.commands.converters.impl.FormattedTimestamp
import dev.kord.common.entity.Snowflake
import dev.kord.core.cache.data.GuildData
import dev.kord.core.entity.Guild
import dev.kord.core.entity.User
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant


object PunishmentLogTable: LongIdTable() {
	val guild = varchar("guild", 256) // The guild this took place in
	val expireTime = long("expireTime") // punishment expiration time
	val timeApplied = long("timeApplied") // time punishment was applied
	val reason = varchar("reason", 256)
	val type = varchar("type", 256) // either WARN, MUTE, TIMEOUT, or BAN
	val punisher = varchar("punisher", 256) // ID of the person who applied the punishment
	val target = varchar("target", 256) // ID of the person who was punished
}


private fun logPunishmentToDatabase(
	data: Punishment
) {

}

/**
 * Logs a punishment to the database, and
 * logs it in the guild's configured channel
 */
fun logPunishment(data: Punishment) {

}




data class Punishment(
	val guild: Snowflake,
	val punisher: Snowflake,
	val target: Snowflake,
	val type: PunishmentType,
	val reason: String,
	val expireTime: Instant,
	val timeApplied: Instant) {
	val expired: Boolean
		get() = expireTime.isBefore(Instant.now())
}

/**
 * Global (cross-guild) punishments for this user
 */
val User.punishments: Set<Punishment>
	get() = transaction {
			val punishments = mutableListOf<Punishment>()
			PunishmentLogTable.select { PunishmentLogTable.target eq this@punishments.id.value.toString() }.forEach {row ->
				punishments.add(
					Punishment(
						Snowflake(row[PunishmentLogTable.guild]),
						Snowflake(row[PunishmentLogTable.punisher]),
						Snowflake(row[PunishmentLogTable.target]),
						PunishmentType.valueOf(row[PunishmentLogTable.type]),
						row[PunishmentLogTable.reason],
						Instant.ofEpochSecond(row[PunishmentLogTable.expireTime]),
						Instant.ofEpochSecond(row[PunishmentLogTable.timeApplied]),
					)
				)
			}
			return@transaction punishments.toSet()
		}

enum class PunishmentType {
	WARN, MUTE, TIMEOUT, KICK, BAN
}