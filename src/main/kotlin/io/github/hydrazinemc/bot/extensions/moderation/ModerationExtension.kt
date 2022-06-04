package io.github.hydrazinemc.bot.extensions.moderation

import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.stringChoice
import com.kotlindiscord.kord.extensions.commands.converters.impl.FormattedTimestamp
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingTimestamp
import com.kotlindiscord.kord.extensions.commands.converters.impl.long
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.commands.converters.impl.user
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.time.TimestampType
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.UserBehavior
import dev.kord.rest.builder.message.create.embed
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class ModerationExtension : Extension() {
	override val name: String = "moderation"

	override suspend fun setup() {

		publicSlashCommand(::PunishCommandArgs) {
			name = "punish"
			description = "Punish a user"

			// TODO: change this to a role; people might want to let mods use this but not discord builtin mute
			check { hasPermission(Permission.MuteMembers) }

			action {
				val data = Punishment(
					null,
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
				respond { content = "Not yet implemented" }
			}
		}

		publicSlashCommand(::PardonCommandArgs) {
			name = "pardon"
			description = "Pardon a punishment"
			action {
				val pun = getPunishment(arguments.id)
				// Need to handle: punishment not in guild, punishment pardoned, punishment doesn't exist
			}
		}

		publicSlashCommand(::ListCommandArgs) {
			name = "list-punishments"
			description = "List a user's punishments"

			action {
				var text = ""
				arguments.user.punishments.forEach {
					text += it.getFormattedText()
				}
				respond { embed {
					title = "${arguments.user.username}'s punishments"
					description = text
				} }
			}
		}
	}
	private fun punish(data: Punishment) {
		logPunishmentToDatabase(data)
	}

	inner class PardonCommandArgs : Arguments() {
		val id by long {
			name = "punishment-id"
			description = "The id of the punishment to pardon"
		}
	}

	inner class ListCommandArgs : Arguments() {
		val user by user {
			name = "user"
			description = "The user whose punishments to list"
		}
	}

	inner class PunishCommandArgs : Arguments() {
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
