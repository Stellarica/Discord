package io.github.hydrazinemc.bot.extensions

import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.stringChoice
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.Permission
import io.github.hydrazinemc.bot.logger

class BotStatusExtension : Extension() {
	override val name: String = "status"

	override suspend fun setup() {
		publicSlashCommand(::StatusCommandArgs) {
			name = "status"
			description = "Set the bot status"

			check { hasPermission(Permission.ManageGuild) }
			action {
				this@publicSlashCommand.kord.editPresence {
					when (arguments.type) {
						"watching" -> watching(arguments.text)
						"listening" -> listening(arguments.text)
						"playing" -> playing(arguments.text)
					}
				}
				logger.info { "Set bot status to ${arguments.type} ${arguments.text}" }
				respond { content = "Set status to ${arguments.type} ${arguments.text}" }
			}
		}
	}

	inner class StatusCommandArgs : Arguments() {
		val type by stringChoice {
			name = "type"
			description = "status type"
			choices = mutableMapOf(
				"watching" to "watching",
				"playing" to "playing",
				"listening" to "listening"
			)
		}
		val text by string {
			name = "text"
			description = "the text to set the status to"
		}
	}
}
