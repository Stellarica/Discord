package io.github.hydrazinemc.bot.extensions.misc

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.int
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.rest.builder.message.create.FollowupMessageCreateBuilder
import dev.kord.rest.builder.message.create.embed
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup

class XKCDExtension: Extension() {
	override val name = "xkcd"

	data class XKCD(
		val num: Int,
		val title: String,
		val alt: String,
		val img: String
	) {
		fun getEmbed(builder: FollowupMessageCreateBuilder) =
			builder.embed {
				this.title = this@XKCD.title
				this.description = this@XKCD.alt
				this.image = img
				this.footer {
					text = if (num == -1) {"latest xkcd"} else {"xkcd #$num"}
				}
			}
	}

	private fun getXKCD(num: Int = -1): XKCD {
		val comic = if (num == -1) {""} else {num}
		val doc =
			try {
				Jsoup.connect("https://xkcd.com/$comic").get().select("#comic img").first()
			}
			catch (e: HttpStatusException) {null}
		val imgurl = doc?.let {"https:" + doc.attr("src") }
		val title = doc?.attr("alt")
		val alt = doc?.attr("title")


		return XKCD(
			num,
			title ?: "no comic title found",
			alt ?: "no alt text found",
			imgurl ?: "https://imgs.xkcd.com/comics/not_available.png"
		)
	}

	override suspend fun setup() {
		publicSlashCommand() {
			name = "xkcd"
			description = "XKCD related commands"
			publicSubCommand {
				name = "latest"
				description = "Gets the latest XKCD comic"
				action {respond { getXKCD().getEmbed(this) } }
			}
			publicSubCommand(::RangeCommandArgs) {
				name = "range"
				description = "Gets a range of XKCD comics"
				action {
					for (num in arguments.first..arguments.last) {
						respond { getXKCD(num).getEmbed(this) }
					}
				}
			}
			publicSubCommand(::SingleCommandArgs) {
				name = "get"
				description = "Get a specific XKCD comic"
				action {respond { getXKCD(arguments.num).getEmbed(this) } }
			}
		}
	}

	inner class RangeCommandArgs: Arguments() {
		val first by int {
			name = "first"
			description = "The first comic to get"
		}
		val last by int {
			name = "last"
			description = "The last comic to get"
		}
	}

	inner class SingleCommandArgs: Arguments() {
		val num by int {
			name = "num"
			description = "The comic to get"
		}
	}
}
