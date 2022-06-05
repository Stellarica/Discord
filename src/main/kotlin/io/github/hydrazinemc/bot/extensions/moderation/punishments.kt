package io.github.hydrazinemc.bot.extensions.moderation

import com.kotlindiscord.kord.extensions.time.TimestampType
import com.kotlindiscord.kord.extensions.time.toDiscord
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.UserBehavior
import io.github.hydrazinemc.bot.getSnowflake
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update


data class Punishment(
	var id: Long? = null,
	var guild: Snowflake,
	var punisher: Snowflake,
	var target: Snowflake,
	var type: PunishmentType,
	var reason: String,
	var expireTime: Instant,
	var timeApplied: Instant,
	var pardoner: Snowflake?
) {
	val expired: Boolean
		get() = expireTime.toEpochMilliseconds() < Clock.System.now().toEpochMilliseconds()
	val pardoned: Boolean
		get() = pardoner != null

	fun getFormattedText(): String {
		return """
			**Punishment ID**: `$id`
			**Punisher**: <@$punisher>
			**Target**: <@$target>
			**Type**: $type (${if (pardoned) "Pardoned" else if (expired) "Expired" else "Active"})
			**Reason**: $reason
			**Expire Time**: ${
			if (expireTime == Instant.DISTANT_FUTURE) {
				"Never"
			} else {
				expireTime.toDiscord(TimestampType.ShortDateTime)
			}
		}
			**Time Applied**: ${timeApplied.toDiscord(TimestampType.ShortDateTime)}
		""".trimIndent() +
				if (pardoned) "\n**Pardoned By**: <@$pardoner>" else ""
	}
}

/**
 * Global (cross-guild) punishments for this user
 */
val UserBehavior.punishments: Set<Punishment>
	get() = getPunishments(PunishmentLogTable.target eq this@punishments.id.value.toString())
 val GuildBehavior.punishments: Set<Punishment>
	get() = getPunishments(PunishmentLogTable.guild eq this@punishments.id.value.toString())

private fun getPunishments(b: Op<Boolean>): Set<Punishment> = transaction {
		val punishments = mutableListOf<Punishment>()
		PunishmentLogTable.select { b }.forEach { row ->
			getPunishment(row)?.let { punishments.add(it) }
		}
		return@transaction punishments.toSet()
	}

fun getPunishment(row: ResultRow?): Punishment? {
	row ?: return null
	return Punishment(
		row[PunishmentLogTable.id].value,
		getSnowflake(row[PunishmentLogTable.guild])!!,
		getSnowflake(row[PunishmentLogTable.punisher])!!,
		getSnowflake(row[PunishmentLogTable.target])!!,
		PunishmentType.valueOf(row[PunishmentLogTable.type]),
		row[PunishmentLogTable.reason],
		Instant.fromEpochMilliseconds(row[PunishmentLogTable.expireTime]),
		Instant.fromEpochMilliseconds(row[PunishmentLogTable.timeApplied]),
		getSnowflake(row[PunishmentLogTable.pardoned]),
	)
}

fun getPunishment(punishmentID: Long): Punishment? = transaction {
	getPunishment(PunishmentLogTable.select { PunishmentLogTable.id eq punishmentID }.firstOrNull())
}

fun updatePunishment(punishmentID: Long, data: Punishment) {
	transaction {
		PunishmentLogTable.update({ PunishmentLogTable.id eq punishmentID}) {
			update(it, data)
		}
	}
}

private fun <t>update(b: UpdateBuilder<t>, data: Punishment) {
	b[PunishmentLogTable.guild] = data.guild.value.toString()
	b[PunishmentLogTable.expireTime] = data.expireTime.toEpochMilliseconds()
	b[PunishmentLogTable.timeApplied] = data.timeApplied.toEpochMilliseconds()
	b[PunishmentLogTable.reason] = data.reason
	b[PunishmentLogTable.type] = data.type.toString()
	b[PunishmentLogTable.punisher] = data.punisher.value.toString()
	b[PunishmentLogTable.target] = data.target.value.toString()
	b[PunishmentLogTable.pardoned] = data.pardoner?.value.toString()
}

object PunishmentLogTable : LongIdTable() {
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


fun logPunishmentToDatabase(data: Punishment): Long? = transaction {
		return@transaction PunishmentLogTable.insert { row ->
			update(row, data)
		}.resultedValues?.firstOrNull()?.get(PunishmentLogTable.id)?.value
	}

enum class PunishmentType { WARN, TIMEOUT, KICK, BAN }
