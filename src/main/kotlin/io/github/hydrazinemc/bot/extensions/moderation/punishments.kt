package io.github.hydrazinemc.bot.extensions.moderation

import com.kotlindiscord.kord.extensions.time.TimestampType
import com.kotlindiscord.kord.extensions.time.toDiscord
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.UserBehavior
import io.github.hydrazinemc.bot.punishmentCollection
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.litote.kmongo.eq
import java.util.UUID

@Serializable
data class Punishment(
	var guild: Snowflake,
	var punisher: Snowflake,
	var target: Snowflake,
	var type: PunishmentType,
	var reason: String,
	var expireTime: Instant,
	var timeApplied: Instant,
	var pardoner: Snowflake?,
	@Contextual
	var id: UUID = UUID.randomUUID(),
) {
	val expired: Boolean
		get() = expireTime.toEpochMilliseconds() < Clock.System.now().toEpochMilliseconds()
	val pardoned: Boolean
		get() = pardoner != null

	fun getFormattedText(): String {
		return """
			**Punishment ID**: `$id`
			**Punisher**: <@$punisher>
			**Target**: <@$target>
			**Type**: $type (${if (pardoned) "Pardoned" else if (expired) "Expired" else "Active"})
			**Reason**: $reason
			**Expire Time**: ${
			if (expireTime == Instant.DISTANT_FUTURE) {
				"Never"
			} else {
				expireTime.toDiscord(TimestampType.ShortDateTime)
			}
		}
			**Time Applied**: ${timeApplied.toDiscord(TimestampType.ShortDateTime)}
		""".trimIndent() +
				if (pardoned) "\n**Pardoned By**: <@$pardoner>" else ""
	}
}

/**
 * Global (cross-guild) punishments for this user
 */
suspend fun UserBehavior.getPunishments(): Set<Punishment> =
	punishmentCollection.find(Punishment::target eq this.id).toList().toSet()


enum class PunishmentType { WARN, TIMEOUT, KICK, BAN }
