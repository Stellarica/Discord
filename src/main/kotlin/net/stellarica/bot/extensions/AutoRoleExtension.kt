package net.stellarica.bot.extensions

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.common.entity.Snowflake
import dev.kord.core.event.guild.MemberJoinEvent

class AutoRoleExtension: Extension() {
	override val name: String = "autorole"
	override suspend fun setup() {
		event<MemberJoinEvent> {
			action {
				this.event.member.addRole(Snowflake(1061090651917275147), "Joined Server")
			}
		}
	}
}