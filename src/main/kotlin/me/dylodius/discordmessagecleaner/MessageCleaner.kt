package me.dylodius.discordmessagecleaner

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageHistory
import net.dv8tion.jda.api.entities.TextChannel
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletableFuture
import kotlin.system.exitProcess

/**
 * Object for handling the message cleanup within a [TextChannel].
 * @property channel The [TextChannel] to cleanup.
 */
class MessageCleaner(private val channel: TextChannel) {

    /**
     * The class' [Logger].
     */
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    /**
     * A [MessageHistory] object containing data on retrieved [Message] history within a [TextChannel].
     * Initially, this will fetch the first 100 Discord messages within the Discord channel.
     *
     * When the initial batch has been processed, the value for this variable will be REPLACED with a
     * new [MessageHistory] object containing data on the next 100 messages of the [TextChannel].
     *
     * We DO NOT use [MessageHistory.retrieveFuture] as this will consume a large amount of resources if the
     * [TextChannel] has many [Message] objects to process.
     */
    private var messageHistory: MessageHistory = channel.getHistoryFromBeginning(100).complete()

    /**
     * A [MutableList] of [Message] objects representing which Discord messages are to be deleted.
     *
     * The process of deleting Discord messages is executed after 50 message batches have been processed, or
     * there are no message batches left to analyse. When a set of message deletions have been processed, this
     * [MutableList] will be cleared via [MutableList.clear], ready for future [Message] objects to be inserted.
     */
    private val messagesToDelete: MutableList<Message> = ArrayList()

    /**
     * Start the target Discord channel cleanup process.
     *
     * Messages are analysed in batches of 100 [Message] objects. After a batch has been completed, the next batch of 100
     * [Message] objects will be requested via a new instantiation of [MessageHistory].
     *
     * Each [Message] in a batch is analysed to see if it contains content from within [BotConfiguration.cleanupWords].
     * This instance which will be used in this process is at [BOT_CONFIGURATION].
     *
     * The implementation that is used to analyse messages is done via [String.contains] against [Message.getContentRaw].
     *
     * In the event that a match is found, it will be marked for deletion by being inserted into [messagesToDelete].
     */
    fun start() {
        /**
         * The current batch iteration.
         *
         * This variable will be incremented by 1 at the start of a [Message] batch's analysis.
         *
         * This variable will be set back to 0 when 50 batch iterations have occurred.
         */
        var batchIteration = 0

        /**
         * Start an infinite loop.
         *
         * Will be broken when the cleanup process for the last message within the target channel has finished up.
         */
        while (true) {
            /**
             * A [List] containing [Message] objects which are to be analysed.
             *
             * This list is reversed from what is retried from [MessageHistory.getRetrievedHistory] to ensure
             * [Message.getContentRaw] data is analysed in a "oldest-first" fashion.
             */
            val messages: List<Message> = messageHistory.retrievedHistory.asReversed()

            batchIteration++

            logger.info("Scanning new message batch.")

            /**
             * For each word in [BotConfiguration.cleanupWords] in the created [BotConfiguration] instance,
             * loop through each [Message] object in the message batch to see if [Message.getContentRaw] contains
             * the cleanup word.
             *
             * This process is not case-sensitive, nor space-sensitive.
             *
             * If a cleanup word has been matched, it'll be inserted into [messagesToDelete] to mark it for deletion.
             */
            for (word in BOT_CONFIGURATION.cleanupWords) {
                for (message in messages) {
                    if (message.contentRaw.contains(other = word, ignoreCase = true)) {
                        messagesToDelete.add(message)
                        logger.info("FLAGGED: {}", message.contentRaw)
                    }
                }
            }

            /**
             * In the event there has been 50 batch iterations, execute [deleteMessageBatch] and set the batch iteration
             * back to 0.
             */
            if (batchIteration == 50) {
                deleteMessageBatch()
                batchIteration = 0
            }

            val latestScannedMessageId = messages[messages.size - 1].idLong

            /**
             * Check to see if the latest analysed [Message.getIdLong] is equal to the last [Message.getIdLong] within
             * the channel. If so, break.
             *
             * If the above isn't the case, fetch a new [MessageHistory] object for messages AFTER the last scanned
             * message ID. This operation will yield until the object has been fetched. The loop will then start again.
             */
            if (latestScannedMessageId == channel.latestMessageIdLong) {
                break
            } else {
                logger.info("Fetching new message batch.")
                messageHistory = channel.getHistoryAfter(latestScannedMessageId, 100).complete()
            }
        }

        /** Execute [deleteMessageBatch] for any remaining [Message] objects pending deletion. */
        deleteMessageBatch()

        /** Finished! Exit application. */
        logger.info("Message cleanup finished. Exiting...")
        exitProcess(0)
    }

    /**
     * Process any [Message] objects marked for deletion.
     */
    private fun deleteMessageBatch() {
        /**
         * A [List] containing [CompletableFuture] objects providing the progress of [Message] objects pending
         * deletion.
         */
        val messageDeletionFutures: List<CompletableFuture<Void>> = channel.purgeMessages(messagesToDelete)

        /**
         * Start an infinite-loop.
         *
         * Each iteration will state progress on how many [CompletableFuture] objects are yet to be marked as
         * [CompletableFuture.isDone].
         *
         * There is a 200ms delay between each iteration, to avoid console spam.
         *
         * Will be broken when all [CompletableFuture] objects are [CompletableFuture.isDone], in which
         * [messagesToDelete] will be cleared via [MutableList.clear] also.
         */
        while (true) {
            val pendingFutureCount = messageDeletionFutures.filter {
                !it.isDone
            }.count()

            if (pendingFutureCount == 0) {
                logger.info("Message deletion futures completed.")
                messagesToDelete.clear()
                break
            } else {
                logger.info("Waiting for {} message deletion futures to complete.", pendingFutureCount)
                Thread.sleep(200)
            }
        }
    }
}