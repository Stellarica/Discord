package io.github.hydrazinemc.bot.extensions.moderation

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.entity.User
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction


data class Punishment(
	val id: Long? = null,
	val guild: Snowflake,
	val punisher: Snowflake,
	val target: Snowflake,
	val type: PunishmentType,
	val reason: String,
	val expireTime: Instant,
	val timeApplied: Instant,
	val pardoner: Snowflake?) {
	val expired: Boolean
		get() = expireTime.toEpochMilliseconds() < Clock.System.now().toEpochMilliseconds()
	val pardoned: Boolean
		get() = pardoner != null
}

/**
 * Global (cross-guild) punishments for this user
 */
private val User.punishments: Set<Punishment>
	get() = transaction {
		val punishments = mutableListOf<Punishment>()
		PunishmentLogTable.select { PunishmentLogTable.target eq this@punishments.id.value.toString() }.forEach { row ->
			getPunishment(row)?.let { punishments.add(it) }
		}
		return@transaction punishments.toSet()
	}

private val GuildBehavior.punishments: Set<Punishment>
	get() = transaction {
		val punishments = mutableListOf<Punishment>()
		PunishmentLogTable.select { PunishmentLogTable.guild eq this@punishments.id.value.toString() }.forEach { row ->
			getPunishment(row)?.let { punishments.add(it) }
		}
		return@transaction punishments.toSet()
	}

fun getPunishment(row: ResultRow?): Punishment? {
	row ?: return null
	return Punishment(
		row[PunishmentLogTable.id].value,
		Snowflake(row[PunishmentLogTable.guild]),
		Snowflake(row[PunishmentLogTable.punisher]),
		Snowflake(row[PunishmentLogTable.target]),
		PunishmentType.valueOf(row[PunishmentLogTable.type]),
		row[PunishmentLogTable.reason],
		Instant.fromEpochMilliseconds(row[PunishmentLogTable.expireTime]),
		Instant.fromEpochMilliseconds(row[PunishmentLogTable.timeApplied]),
		Snowflake(row[PunishmentLogTable.pardoned]),
	)
}

fun getPunishment(punishmentID: Long) : Punishment? = transaction {
	getPunishment(PunishmentLogTable.select { PunishmentLogTable.id eq punishmentID }.firstOrNull())
}

object PunishmentLogTable: LongIdTable() {
	val guild = varchar("guild", 256) // The guild this took place in
	val expireTime = long("expireTime") // punishment expiration time
	val timeApplied = long("timeApplied") // time punishment was applied
	val reason = varchar("reason", 256)
	val type = varchar("type", 256) // either WARN, MUTE, TIMEOUT, or BAN
	val punisher = varchar("punisher", 256) // ID of the person who applied the punishment
	val target = varchar("target", 256) // ID of the person who was punished
	val pardoned = varchar("pardoned", 256) // ID of the user who pardoned the punishment,
	// if this has not been stored, the punishment hasn't been pardoned.
}


private fun logPunishmentToDatabase(
	data: Punishment
) {
	transaction {
		PunishmentLogTable.insert { row ->
			row[guild] = data.guild.value.toString()
			row[expireTime] = data.expireTime.toEpochMilliseconds()
			row[timeApplied] = data.timeApplied.toEpochMilliseconds()
			row[reason] = data.reason
			row[type] = data.type.toString()
			row[punisher] = data.punisher.value.toString()
			row[target] = data.target.value.toString()
			row[pardoned] = data.pardoner?.value.toString()
		}
	}
}

enum class PunishmentType {
	WARN, MUTE, TIMEOUT, KICK, BAN
}
