package net.stellarica.bot.extensions
import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.stringChoice
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.Permission
import com.kotlindiscord.kord.extensions.utils.capitalizeWords
import dev.kord.rest.builder.message.create.embed
import net.stellarica.bot.logger
class Sparkles3421Commands : Extension() {
    override val name: String = "Sparkles3421Commands"
    override suspend fun setup() {
        publicSlashCommand(::Sparkles3421InfoArg) {
            name = "info"
            description = "Get Info"
            action {
                var con = "An internal error has occured"
                if (arguments.target) {
                    when(arguments.target) {
                        "website" -> con = "The stellarica website is: https://stellarica.net \n The wiki is: https://wiki.stellarica.net \n The information panel is: https://sparkles3421.github.io/stellarica/info/"
                        "discord" -> con = "The discord is https://discord.com/invite/sjMY88Wwf8"
                        else -> con = "Please set target to 'website' or 'discord'!"
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
    inner class Sparkles3421InfoArg : Arguments() {
        val target by defaultingString  {
            name = "target"
            description = "website/discord"
            defaultValue = "website"
        }
    }

}
