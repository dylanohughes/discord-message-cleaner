# Introduction
Sometimes, Discord Community Managers or Administrators wish to cleanup a Discord channel's messages due to certain circumstances such as toxicity, spamming, or trolling. This Discord bot intends to help this process by scanning all messages within a channel, removing those which contain any of the set (configurable) filtration words.

# Requirements
- Java 6+

# Getting Started
1. Download the latest released `.jar` from the [releases](https://github.com/quinnicious/discord-message-cleaner/releases) page.
2. Move the downloaded `.jar` somewhere easy to find, such as within a folder on your Desktop.
3. **Do not** double-click the `.jar` file. Instead, do the following.
   1. Open a command prompt.
   2. Navigate to the directory where you stored the `.jar` file by using the command `cd path/to/directory`.
   3. Execute the `.jar` file by using the command `java -jar discord-message-cleaner.jar`.
4. For first startup or instances where a configuration file could not be found, one will be generated within the directory where the `.jar` file is located. The application will exit automatically to allow you the opportunity to edit it. See the section on "Configuration" for details.
5. Once you have modified the configuration file to your liking, execute the `.jar` file again (see above for a reminder as to what the command is).
6. The application will start cleaning up the target Discord channel.

# Configuration
The following fields are found within the `config.yml` file in the same directory where the `.jar` file is located after first startup.
- `bot_token` - The token which allows the application to login as a Discord bot. See [this guide](https://www.writebots.com/discord-bot-token/]) on how to create and add a bot to your Discord server.
    - The bot account requires the following permissions: `Read Message History`, `Manage Messages`, or alternatively, these can be set for an individual channel when the bot has joined the server.
- `guild_id` - The target guild ID (snowflake) which contains the channel to clean up.
- `channel_id` - The target channel ID (snowflake) to clean up.
- `cleanup_words` - A list of words where, if a message contains one, it will be marked for deletion. The provided words are not case case-sensitive.

# Open-Source Dependencies
This application is built using the following open-source dependencies. Much appreciated.
- [Kotlin](https://github.com/JetBrains/kotlin)
- [SLF4J](https://github.com/qos-ch/slf4j)
- [JDA (Java Discord API)](https://github.com/DV8FromTheWorld/JDA)
- [SnakeYaml](https://bitbucket.org/asomov/snakeyaml/src/master/)
