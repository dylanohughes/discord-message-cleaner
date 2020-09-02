package me.dylodius.discordmessagecleaner

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

/**
 * The bot configuration data.
 * Fetched on initialisation.
 */
class BotConfiguration {

    /**
     * The class' [Logger].
     */
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    /**
     * The configured bot token.
     */
    val botToken: String

    /**
     * The configured target guild ID.
     */
    val guildId: Long

    /**
     * The configured target channel ID.
     */
    val channelId: Long

    /**
     * The configured cleanup words.
     */
    val cleanupWords: List<String>

    /**
     * Load data from the configuration file.
     *
     * If configuration file doesn't exist within the directory where the jar file is located, it'll be generated and
     * the application will exit so the file can be edited by the user.
     */
    init {
        val yaml = Yaml()
        val inputStream: InputStream = try {
            File("./config.yml").inputStream()
        } catch (e: FileNotFoundException) {
            Files.copy(
                ClassLoader.getSystemClassLoader().getResourceAsStream("config.yml")!!,
                Paths.get("./config.yml")
            )

            logger.info("Generated configuration file. Please see jar directory, edit file, and restart bot. Exiting...")
            exitProcess(0)
        }

        val rootObject: Map<String, Any> = yaml.load(inputStream)
        inputStream.close()

        botToken = getValue(rootObject, "bot_token") as String
        guildId = getValue(rootObject, "guild_id") as Long
        channelId = getValue(rootObject, "channel_id") as Long
        cleanupWords = (getValue(rootObject, "cleanup_words") as List<*>).filterIsInstance<String>()
    }

    /**
     * Safely get a value from [Yaml.load].
     *
     * If the value exists, it will be returned, although not in its correct data type. It will need casting.
     *
     * If the value doesn't exist, the program will exit.
     */
    private fun getValue(rootObject: Map<String, Any>, key: String): Any {
        val value: Any? = rootObject[key]

        if (value != null) {
            return value
        } else {
            logger.info(
                "Key \"{}\" could not be found within the configuration file. This indicates you have incorrect formatting, or it's an outdated file. Try deleting and regenerating. Exiting...",
                key
            )
            exitProcess(0)
        }
    }
}