package net.stellarica.bot.extensions
import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.stringChoice
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.capitalizeWords
import dev.kord.common.entity.Permission
import dev.kord.rest.builder.message.create.embed
import net.stellarica.bot.logger
class Sparkles3421Commands : Extension() {
    override suspend fun setup() {
        publicSlashCommand(::SlapSlashArgs) {
            name = "info"
            description = "Get Info"
            guild(TEST_SERVER_ID)
            action {
                val kord = this@TestExtension.kord
                val realTarget = if (arguments.target.id == kord.selfId) {
                    member
                } else {
                    arguments.target
                }
                var con = "An internal error has occured"
                if (arguments.target) {
                    if (arguments.target == "website") {
                        con = """The stellarica website is: https://stellarica.net
                            The wiki is: https://wiki.stellarica.net
                            The information panel is: https://sparkles3421.github.io/stellarica/info/
                        """.trimMargin()
                    } else if (arguments.target == "discord") {
                        con = "The discord is https://discord.com/invite/sjMY88Wwf8"
                    } else {
                        con = "Please set target to 'website' or 'discord'!"
                    }
                } else {
                    con = "Please set a target"
                }
                respond {
                    content = con;
                }
            }
        }
    }
    inner class Sparkles3421Commands : Arguments() {
        val weapon by defaultingString {
            name = "target"
            description = "website/discord"
            defaultValue = "website"
        }
    }

}
