package io.github.hydrazinemc.bot.extensions

import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.stringChoice
import com.kotlindiscord.kord.extensions.commands.converters.impl.FormattedTimestamp
import com.kotlindiscord.kord.extensions.commands.converters.impl.channel
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingTimestamp
import com.kotlindiscord.kord.extensions.commands.converters.impl.snowflake
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.commands.converters.impl.timestamp
import com.kotlindiscord.kord.extensions.commands.converters.impl.user
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.time.TimestampType
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.Permission
import io.github.hydrazinemc.bot.database.GuildConfigTable
import io.github.hydrazinemc.bot.database.botLogChannel
import io.github.hydrazinemc.bot.database.punishmentLogChannel
import io.github.hydrazinemc.bot.database.setGuildConfig
import kotlinx.datetime.Instant

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
			name = "mute"
			description = "Mute a user"

			// TODO: change this to a role; people might want to let mods use this but not discord builtin mute
			check { hasPermission(Permission.MuteMembers) }
			action {
				respond { content = "Not yet implemented" }
			}
		}
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

	inner class ModerationCommandArgs : Arguments() {
		val subject by user {
			name = "user"
			description = "The user in question"
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