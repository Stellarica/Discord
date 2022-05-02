package io.github.hydrazinemc.bot.extensions

import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.stringChoice
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.Permission
import io.github.hydrazinemc.bot.database.GuildConfigTable
import io.github.hydrazinemc.bot.database.setGuildConfig

class ModerationExtension : Extension() {
	override val name: String = "moderation"

	override suspend fun setup() {
		publicSlashCommand(::GuildConfigArgs) {
			name = "config"
			description = "Configure bot settings"

			check { hasPermission(Permission.ManageGuild) }
			action {
				/*, when (arguments.option) {
					"pc" -> GuildConfigTable.punishmentLogChannel
					"blc" -> GuildConfigTable.botLogChannel
					else -> {
						respond { content = "Somehow you chose an invalid option. This shouldn't be possible, and is a bug" }
						return@action
					}
				}, arguments.value)

				 */
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
		val value by string {
			name = "value"
			description = "The new value"
		}
	}
}