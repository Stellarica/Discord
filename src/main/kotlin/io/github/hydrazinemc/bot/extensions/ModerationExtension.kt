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
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

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

private fun logPunishmentToChannel(data: Punishment) {

}


/**
 * Logs a punishment to the database, and
 * logs it in the guild's configured channel
 */
private fun logPunishment(data: Punishment) {
	if (data.expired) {
		logger.warn{"Logging expired punishment"}
	}
	logPunishmentToDatabase(data)
	logPunishmentToChannel(data)
}

private data class Punishment(
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
		PunishmentLogTable.select { PunishmentLogTable.target eq this@punishments.id.value.toString() }.forEach {row ->
			punishments.add(
				Punishment(
					Snowflake(row[PunishmentLogTable.guild]),
					Snowflake(row[PunishmentLogTable.punisher]),
					Snowflake(row[PunishmentLogTable.target]),
					PunishmentType.valueOf(row[PunishmentLogTable.type]),
					row[PunishmentLogTable.reason],
					Instant.fromEpochMilliseconds(row[PunishmentLogTable.expireTime]),
					Instant.fromEpochMilliseconds(row[PunishmentLogTable.timeApplied]),
					Snowflake(row[PunishmentLogTable.pardoned]),
				)
			)
		}
		return@transaction punishments.toSet()
	}

private enum class PunishmentType {
	WARN, MUTE, TIMEOUT, KICK, BAN
}
