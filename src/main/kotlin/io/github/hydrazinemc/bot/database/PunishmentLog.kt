package io.github.hydrazinemc.bot.database

import org.jetbrains.exposed.dao.id.LongIdTable


object PunishmentLogTable: LongIdTable() {
	val guild = varchar("guild", 256) // The guild this took place in
	val duration = integer("duration") // punishment duration in seconds
	val timeApplied = long("timeApplied") // time punishment was applied
	val reason = varchar("reason", 256)
	val type = varchar("type", 256) // either WARN, MUTE, TIMEOUT, or BAN
	val punisher = varchar("punisher", 256) // ID of the person who applied the punishment
	val target = varchar("target", 256) // ID of the person who was punished
}
