import com.kotlindiscord.kord.extensions.checks.hasRole
import com.kotlindiscord.kord.extensions.checks.notHasRole
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.channel
import com.kotlindiscord.kord.extensions.components.applyComponents
import com.kotlindiscord.kord.extensions.components.components
import com.kotlindiscord.kord.extensions.components.forms.ModalForm
import com.kotlindiscord.kord.extensions.components.publicButton
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.modules.unsafe.annotations.UnsafeAPI
import com.kotlindiscord.kord.extensions.modules.unsafe.extensions.unsafeSubCommand
import com.kotlindiscord.kord.extensions.modules.unsafe.types.InitialSlashCommandResponse
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.modal
import dev.kord.core.entity.channel.Channel
import dev.kord.core.entity.channel.TextChannel
import dev.kord.rest.builder.message.modify.embed
import net.stellarica.bot.config
import net.stellarica.bot.saveConfig

class TesterApplicationExtension: Extension() {
	val testerRole = Snowflake(1044828684101632110)
	override val name: String = "testerapplication"
	a
	override suspend fun setup() {
		ephemeralSlashCommand() {
			name = "tester"
			description = "Tester application related commands"
			unsafeSubCommand() {
				name = "apply"
				description = "Apply to be a tester"
				initialResponse = InitialSlashCommandResponse.None
				check {
					notHasRole(testerRole)
				}

				action {
					val modalObj = ApplicationModal()

					this@unsafeSubCommand.componentRegistry.register(modalObj)

					event.interaction.modal(
						modalObj.title,
						modalObj.id
					) {
						modalObj.applyToBuilder(this, getLocale(), null)
					}

					modalObj.awaitCompletion { modalSubmitInteraction ->
						interactionResponse = modalSubmitInteraction?.deferEphemeralMessageUpdate()!!
					}

					val message = (guild!!.getApplicationChannel() as TextChannel).createEmbed {
						color = Color(0x22aaaa)
						title = "New Tester Application"
						field {
							name = "Minecraft Username"
							value = modalObj.minecraftUserName.value!!
						}
					}
					message.edit {
						applyComponents(components {
							publicButton {
								label = "Accept Application"
								action {
									user.getDmChannel().createEmbed {
										color = Color(0x22aaaa)
										title = "Tester Application Accepted"
										description = "Your tester application has been accepted! Feel free to ask in #tester-chat if you have any questions"
									}
									this@app.action.user.asMember(guild!!.id).addRole(testerRole)
								}
							}
						})
					}
				}
			}

			ephemeralSubCommand(::SetChannelArgs) {
				name = "set-channel"
				description = "Set the channel that receives tester applications"
				action {
					config.testerChannel.set(guild!!.id, arguments.channel.id)
					saveConfig()
					respond {
						content = "Set application channel to ${guild!!.getApplicationChannel()!!.mention}"
					}
				}
			}
		}
	}

	inner class SetChannelArgs: Arguments() {
		val channel: Channel by channel {
			name = "channel"
			description = "The channel to send applications to"
		}
	}

	suspend fun GuildBehavior.getApplicationChannel(): Channel? {
		val id = config.testerChannel[this.id] ?: return null
		return this.getChannelOrNull(id)
	}

	inner class ApplicationModal: ModalForm() {
		override var title = "Stellarica Tester Application"

		val minecraftUserName = lineText {
			label = "Minecraft Username"
			placeholder = "Enter your Minecraft username"
		}
	}
}