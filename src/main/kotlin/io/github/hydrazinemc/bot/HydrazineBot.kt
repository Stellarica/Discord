package io.github.hydrazinemc.bot

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.utils.env
import dev.kord.common.entity.PresenceStatus
import dev.kord.common.entity.Snowflake
import io.github.hydrazinemc.bot.database.GuildConfigTable
import io.github.hydrazinemc.bot.extensions.ModerationExtension
import io.github.hydrazinemc.bot.extensions.PunishmentLogTable
import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Slf4jSqlDebugLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.sql.Connection

val TEST_SERVER_ID = Snowflake(  // Store this as a Discord snowflake, aka an ID
	env("TEST_SERVER")  // An exception will be thrown if it can't be found
)
val logger = KotlinLogging.logger {}

suspend fun main() {

	Database.connect("jdbc:sqlite:data.db", "org.sqlite.JDBC")
	TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE

	newSuspendedTransaction {
		addLogger(Slf4jSqlDebugLogger)
		SchemaUtils.create(PunishmentLogTable, GuildConfigTable)
	}

	val bot = ExtensibleBot(env("TOKEN")) {
		applicationCommands {
			enabled = true
			defaultGuild = TEST_SERVER_ID
		}
		extensions {
			add(::ModerationExtension)
		}
		presence {
			status = PresenceStatus.DoNotDisturb
			// Can be changed later
			watching("great things happen")
		}
	}
	bot.start()
}