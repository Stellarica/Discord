package net.stellarica.bot

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.utils.env
import dev.kord.common.entity.PresenceStatus
import dev.kord.common.entity.Snowflake
import net.stellarica.bot.extensions.BotStatusExtension
import mu.KotlinLogging


val TEST_SERVER_ID = Snowflake(  // Store this as a Discord snowflake, aka an ID
	env("TEST_SERVER")  // An exception will be thrown if it can't be found
)
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
		}
		presence {
			status = PresenceStatus.DoNotDisturb
			// Can be changed later
			watching("great things happen")
		}
	}
	bot.start()
}
