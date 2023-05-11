package net.stellarica.bot

import TesterApplicationExtension
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.utils.env
import dev.kord.common.entity.PresenceStatus
import dev.kord.common.entity.Snowflake
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import net.stellarica.bot.extensions.BotStatusExtension
import mu.KotlinLogging
import net.stellarica.bot.extensions.AutoRoleExtension
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.writeText

val logger = KotlinLogging.logger {}
val configPath = Path.of("config.json")
lateinit var config: Config

@OptIn(ExperimentalSerializationApi::class)
suspend fun main() {

	if (configPath.exists()) {
		config = Json.decodeFromStream(configPath.inputStream())
	} else {
		config = Config()
		saveConfig()
	}


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
			add(::TesterApplicationExtension)
		}
		presence {
			status = PresenceStatus.DoNotDisturb
			// Can be changed later
			watching("great things happen")
		}
	}
	bot.start()

}


fun saveConfig() {
	configPath.writeText(Json.encodeToString(Config.serializer(), config))
}
