package net.stellarica.bot

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.utils.env
import dev.kord.common.entity.PresenceStatus
import dev.kord.common.entity.Snowflake
import net.stellarica.bot.extensions.BotStatusExtension
import mu.KotlinLogging
import net.stellarica.bot.extensions.AutoRoleExtension

val logger = KotlinLogging.logger {}

suspend fun main() {

	val bot = ExtensibleBot(env("TOKEN")) {
		applicationCommands {
			enabled = true
			try {
				defaultGuild = Snowflake(env("TEST_SERVER").toLong())
			} catch (_: RuntimeException) {
			} // no default guild
		}
		extensions {
			add(::BotStatusExtension)
			add(::AutoRoleExtension)
			add(::Sparkles3421Commands)
		}
		presence {
			status = PresenceStatus.DoNotDisturb
			// Can be changed later
			watching("great things happen")
		}
	}
	bot.start()
}
