package io.github.hydrazinemc.bot.extensions

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update

class TestExtension: Extension() {
	override val name = "test"

	override suspend fun setup() {
		// We'll do all our setup tasks here

		publicSlashCommand(::DatabaseTestArgs) {  // Pass the arguments class constructor in here
			name = "store"
			description = "Store something in the database"

			action {
				newSuspendedTransaction {
					TestThing.insert {
						it[someText] = arguments.someString
					}
				}
				respond {
					content = "a thing happened"
				}
			}
		}


		publicSlashCommand() {  // Pass the arguments class constructor in here
			name = "get"
			description = "Get stuff from the database"

			action {
				newSuspendedTransaction {
					TestThing.update {
						with(SqlExpressionBuilder) {
							it.update(TestThing.timesRead, TestThing.timesRead + 1)
						}
					}
					respond {
						TestThing.selectAll().forEach {
							content += it[TestThing.someText]
						}
					}
				}
			}
		}
	}

	inner class DatabaseTestArgs: Arguments() {
		val someString by string {
			name = "something"
			description = "some string"
		}
	}
}


object TestThing: IntIdTable() {
	val someText = varchar("someText", 255)
	val timesRead = integer("timesRead").default(0)
}