package me.dylodius.discordmessagecleaner

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.TextChannel
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

/** The loaded bot configuration in the form of a [BotConfiguration] object. */
val BOT_CONFIGURATION = BotConfiguration()

fun main() {

    /**
     * The class' [Logger].
     */
    val logger: Logger = LoggerFactory.getLogger("Main")

    /**
     * A [JDA] object using its light constructor to save resources.
     * This will block the thread until JDA is ready. @see [JDA.awaitReady].
     */
    val jda: JDA = JDABuilder.createLight(BOT_CONFIGURATION.botToken).build().awaitReady()

    /**
     * The [Guild] object representing the target Discord guild ID provided in the bot configuration object.
     * If the target guild ID is invalid, then the program will exit.
     */
    val targetGuild: Guild? = jda.getGuildById(BOT_CONFIGURATION.guildId)
    if (targetGuild == null) {
        logger.info(
            "Target guild of ID \"{}\" doesn't exist or client isn't a member. Exiting...",
            BOT_CONFIGURATION.guildId
        )
        exitProcess(0)
    }

    /**
     * The [TextChannel] object representing the target Discord text channel ID provided in the bot configuration object.
     * If the target channel ID is invalid, then the program will exit.
     */
    val targetTextChannel: TextChannel? = targetGuild.getTextChannelById(BOT_CONFIGURATION.channelId)
    if (targetTextChannel == null) {
        logger.info(
            "Target text channel of ID \"{}\" doesn't exist or client is lacking permissions. Exiting...",
            BOT_CONFIGURATION.channelId
        )
        exitProcess(0)
    }

    /** Construct a [MessageCleaner] object and start the cleanup process. */
    MessageCleaner(targetTextChannel).start()
}