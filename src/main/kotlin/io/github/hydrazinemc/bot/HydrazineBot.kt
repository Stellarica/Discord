package io.github.hydrazinemc.bot

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.utils.env
import com.mongodb.MongoClientSettings
import dev.kord.common.entity.PresenceStatus
import dev.kord.common.entity.Snowflake
import io.github.hydrazinemc.bot.extensions.BotStatusExtension
import io.github.hydrazinemc.bot.extensions.config.GuildConfigExtension
import io.github.hydrazinemc.bot.extensions.moderation.ModerationExtension
import io.github.hydrazinemc.bot.extensions.moderation.Punishment
import mu.KotlinLogging
import org.bson.UuidRepresentation
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo


val TEST_SERVER_ID = Snowflake(  // Store this as a Discord snowflake, aka an ID
	env("TEST_SERVER")  // An exception will be thrown if it can't be found
)
val logger = KotlinLogging.logger {}

val client = KMongo.createClient(
	MongoClientSettings.builder()
		.uuidRepresentation(UuidRepresentation.STANDARD)
		.build()
).coroutine

val database = client.getDatabase("hydrazinebot")

val configCollection = database.getCollection<GuildConfig>("guildconfigs")
val punishmentCollection = database.getCollection<Punishment>("punishments")

suspend fun main() {

	val bot = ExtensibleBot(env("TOKEN")) {
		applicationCommands {
			enabled = true
			defaultGuild = TEST_SERVER_ID
		}
		extensions {
			add(::ModerationExtension)
			add(::BotStatusExtension)
			add(::GuildConfigExtension)
		}
		presence {
			status = PresenceStatus.DoNotDisturb
			// Can be changed later
			watching("great things happen")
		}
	}
	bot.start()
}
