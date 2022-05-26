package io.github.hydrazinemc.bot.extensions

import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.stringChoice
import com.kotlindiscord.kord.extensions.commands.converters.impl.FormattedTimestamp
import com.kotlindiscord.kord.extensions.commands.converters.impl.channel
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingTimestamp
import com.kotlindiscord.kord.extensions.commands.converters.impl.int
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.commands.converters.impl.user
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.time.TimestampType
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.User
import io.github.hydrazinemc.bot.database.botLogChannel
import io.github.hydrazinemc.bot.database.punishmentLogChannel
import io.github.hydrazinemc.bot.logger
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class ModerationExtension : Extension() {
	override val name: String = "moderation"

	override suspend fun setup() {
		publicSlashCommand(::GuildConfigArgs) {
			name = "config"
			description = "Configure bot settings"
			check { hasPermission(Permission.ManageGuild) }

			action {
				// ngl this seems like purified jank
				var thing = when (arguments.option) {
					"pc" -> guild!!.punishmentLogChannel
					"blc" -> guild!!.botLogChannel
					else -> {
						respond { content = "Somehow you chose an invalid option. This shouldn't be possible, and is a bug" }
						return@action
					}
				}
				thing = arguments.value.id
				respond { content = "Set ${arguments.option} to ${arguments.value.mention}" }
			}
		}

		publicSlashCommand(::ModerationCommandArgs) {
			name = "punish"
			description = "Punish a user"

			// TODO: change this to a role; people might want to let mods use this but not discord builtin mute
			check { hasPermission(Permission.MuteMembers) }

			action {
				val data = Punishment(
					guild!!.id,
					user.id,
					arguments.subject.id,
					PunishmentType.valueOf(arguments.action),
					arguments.reason,
					arguments.expireTime!!.instant,
					Clock.System.now(),
					null
				)
				punish(data)
				logPunishment(data)
				respond { content = "Not yet implemented" }
			}
		}

		publicSlashCommand(::PardonCommandArgs) {
			name = "pardon"
			description = "Pardon a punishment"
			action {
				PunishmentLogTable.select { PunishmentLogTable.id eq this@publicSlashCommand.arguments.id }.forEach {row ->
					// Need to handle: punishment not in guild, punishment pardoned, punishment doesn't exist
				}
			}

		}
	}
	private fun punish(data: Punishment) {
	}

	inner class GuildConfigArgs : Arguments() {
		val option by stringChoice {
			name = "option"
			description = "The configuration option to set"
			choices = mutableMapOf(
				"punishment-channel" to "pc",
				"bot-log-channel" to "blc"
			)
		}
		val value by channel {
			name = "value"
			description = "The new value"
		}
	}

	inner class PardonCommandArgs : Arguments() {
		val id by int {
			name = "punishment id"
			description = "The id of the punishment to pardon"
		}
	}

	inner class ModerationCommandArgs : Arguments() {
		val subject by user {
			name = "user"
			description = "The user in question"
		}
		val action by stringChoice { name = "action"
			description = "The action to take"
			choices = mutableMapOf(
				"mute" to "MUTE",
				"kick" to "KICK",
				"timeout" to "TIMEOUT",
				"ban" to "BAN"
			)
		}
		val reason by string {
			name = "reason"
			description = "The reasoning behind this action"
		}
		val expireTime: FormattedTimestamp? by defaultingTimestamp {
			name = "expire"
			description = "The time at which this action expires"
			defaultValue = FormattedTimestamp(Instant.DISTANT_FUTURE, TimestampType.ShortDateTime)
		}
	}
}

object PunishmentLogTable: LongIdTable() {
	val guild = varchar("guild", 256) // The guild this took place in
	val expireTime = long("expireTime") // punishment expiration time
	val timeApplied = long("timeApplied") // time punishment was applied
	val reason = varchar("reason", 256)
	val type = varchar("type", 256) // either WARN, MUTE, TIMEOUT, or BAN
	val punisher = varchar("punisher", 256) // ID of the person who applied the punishment
	val target = varchar("target", 256) // ID of the person who was punished
	val pardoner = varchar("pardoned", 256) // ID of the user who pardoned the punishment,
										// if this has not been stored, the punishment hasn't been pardoned.
}

private fun logPunishmentToChannel(data: Punishment) {

}

private data class Punishment(val id: Long){
	// cache stuff so we aren't spamming as many db transactions.
	// However, this does have side affects. If multiple Punishments of
	// the same ID exist, the cache of one might become outdated
	private var cache = mutableMapOf<Column<Any?>, Any?>()
	private val dbRow : ResultRow?
		get() = PunishmentLogTable.select{ PunishmentLogTable.id eq this@Punishment.id }.firstOrNull()

	private fun <T> set(col: Column<T>, value: T) {
		transaction {
			PunishmentLogTable.update ({PunishmentLogTable.id eq this@Punishment.id}) {
				it[col] = value
			}
		}
		cache[col] = value
	}
	private fun <T> get(col: Column<T>): T? {
		if (cache.containsKey(col)) { return cache[col] as T }
		transaction {
			dbRow?[col]
		}
	}

	var guild: Snowflake?
		get() = get(PunishmentLogTable.guild)?.let { Snowflake(it) }
		set(value) = set(PunishmentLogTable.guild, value?.value.toString())
	var punisher: Snowflake?
		get() = get(PunishmentLogTable.punisher)?.let { Snowflake(it) }
		set(value) = set(PunishmentLogTable.punisher, value?.value.toString())
	var target: Snowflake?
		get() = get(PunishmentLogTable.target)?.let { Snowflake(it) }
		set(value) = set(PunishmentLogTable.target, value?.value.toString())
	var type: PunishmentType?
		get() = get(PunishmentLogTable.type)?.let { PunishmentType.valueOf(it) }
		set(value) = set(PunishmentLogTable.type, value.toString())

	// TODO: fix these because we can't assume the user isn't trying to null them out
	var reason: String?
		get() = get(PunishmentLogTable.reason)
		set(value) = set(PunishmentLogTable.reason, value!!) // this needs help
	var expireTime: Instant?
		get() = get(PunishmentLogTable.expireTime)?.let {Instant.fromEpochMilliseconds(it)}
		set(value) = set(PunishmentLogTable.expireTime, value?.toEpochMilliseconds()!!) // so does this
	var timeApplied: Instant?
		get() = get(PunishmentLogTable.timeApplied)?.let {Instant.fromEpochMilliseconds(it)}
		set(value) = set(PunishmentLogTable.timeApplied, value?.toEpochMilliseconds()!!) // and this

	var pardoner: Snowflake?
		get() = transaction { Snowflake(dbRow?.get(PunishmentLogTable.pardoner) ?: ) }
	val expired: Boolean?
		get() = (expireTime?.toEpochMilliseconds() ?: run { return null }) < Clock.System.now().toEpochMilliseconds()

	val pardoned: Boolean
		get() = pardoner != null
}

/**
 * Global (cross-guild) punishments for this user
 */
private val User.punishments: Set<Punishment>
	get() = transaction {
		val punishments = mutableListOf<Punishment>()
		PunishmentLogTable.select { PunishmentLogTable.target eq this@punishments.id.value.toString() }.forEach {row ->
			punishments.add(Punishment(row[PunishmentLogTable.id]))
		}
		return@transaction punishments.toSet()
	}

private enum class PunishmentType {
	WARN, MUTE, TIMEOUT, KICK, BAN
}
