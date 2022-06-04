package io.github.hydrazinemc.bot.extensions

import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.stringChoice
import com.kotlindiscord.kord.extensions.commands.converters.impl.channel
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.Permission
import io.github.hydrazinemc.bot.database.botLogChannel
import io.github.hydrazinemc.bot.database.punishmentLogChannel

class GuildConfigExtension: Extension() {
	override val name = "config"
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
}
