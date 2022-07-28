package io.github.hydrazinemc.bot.extensions.config

import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.stringChoice
import com.kotlindiscord.kord.extensions.commands.converters.impl.channel
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.Permission
import dev.kord.rest.builder.message.create.embed
import io.github.hydrazinemc.bot.getConfig
import io.github.hydrazinemc.bot.setConfig

class GuildConfigExtension : Extension() {
	override val name = "config"
	override suspend fun setup() {

		publicSlashCommand(::GuildConfigArgs) {
			name = "set-config"
			description = "Configure bot settings"

			check { hasPermission(Permission.ManageGuild) }
			action {
				// ngl this seems like purified jank
				val conf = guild!!.getConfig()
				when (arguments.option) {
					"pc" -> conf.punishmentLogChannel = arguments.value.id
					"blc" -> conf.botLogChannel = arguments.value.id
					else -> {
						respond {
							content = "Somehow you chose an invalid option. This shouldn't be possible, and is a bug"
						}
						return@action
					}
				}
				guild!!.setConfig(conf)
				respond { content = "Set ${arguments.option} to ${arguments.value.mention}" }
			}
		}

		publicSlashCommand {
			name = "get-config"
			description = "Show bot settings"

			check { hasPermission(Permission.ManageGuild) }
			action {
				val conf = guild!!.getConfig()
				respond {
					embed {
						title = "HydrazineBot Configuration"
						description =
							"Punishment Log: <#${conf.punishmentLogChannel}>\nBot Log: <#${conf.botLogChannel}>"
					}
				}
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
