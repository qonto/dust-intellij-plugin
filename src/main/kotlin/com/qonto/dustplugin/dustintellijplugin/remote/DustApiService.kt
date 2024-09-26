package com.qonto.dustplugin.dustintellijplugin.remote

import API_KEY
import USERNAME
import WORKSPACE_ID
import com.intellij.openapi.diagnostic.thisLogger
import com.qonto.dustplugin.dustintellijplugin.remote.models.*
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.json.Json
import java.net.URL
import java.time.Instant
import java.util.*

class DustApiService {

    private val CONVERSATION_ID = "97ABxfSsTv"

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(Logging) { logger = Logger.SIMPLE }
        install(HttpTimeout) {
            requestTimeoutMillis = 10000L
        }
    }

    private fun HttpRequestBuilder.headerBuilder() {
        contentType(ContentType.Application.Json)
        header("authorization", "Bearer $API_KEY")
    }

    suspend fun listAllAssistants(): Result<List<RemoteAssistant>> {
        val url = "$BASE_URL/$ASSISTANTS_PATH"

        val response: HttpResponse = httpClient.get(url) {
            headerBuilder()
        }

        val res = request<RemoteAssistants> {
            response
        }
        res.onSuccess {
            thisLogger().warn(
                "MAHYA:: RESPONSE IS SUCCESS in ✅\n First 10 are: ${
                    res.map {
                        it.agentConfigurations.take(10).map {
                            "NAME: ${it.name} ; ID: ${it.sId}"
                        }
                    }
                }\n Size of agents: ${it.agentConfigurations.size}"
            )
        }

        return res.map { it.agentConfigurations }
    }

    suspend fun createConversation(): Result<ConversationInfo> {
        val url = "$BASE_URL/$CONVERSATION_PATH"

        val response: HttpResponse = httpClient.post(url) {
            headerBuilder()
            setBody("{ \"blocking\": false, \"visibility\": \"unlisted\", \"title\": \"Default\" }")
        }

        val res = request<RemoteConversation> {
            response
        }
        res.onSuccess {
            thisLogger().warn(
                "MAHYA:: Create conversation IS SUCCESS in ✅ ${
                    res.map { it }
                }\n Conversation Id: ${it.conversation.sId}"
            )
        }

        return res.map {
            it.conversation
        }
    }

    suspend fun createMessage(
        conversationId: String = CONVERSATION_ID,
        message: String = "HELLO FROM MAHYA ${Date.from(Instant.now())}",
        assistantId: String = "mistral-large"
    ): Result<RemoteMessage> {
        val url = "$BASE_URL/$CONVERSATION_PATH/$conversationId/messages"

        val response: HttpResponse = httpClient.post(url) {
            headerBuilder()
            setBody(
                "{" +
                        " \"mentions\": [ { \"configurationId\": \"$assistantId\" } ]," +
                        " \"context\": { \"origin\": \"api\", \"username\": \"$USERNAME\", \"timezone\": \"Europe/Paris\", \"fullName\": \"\", \"email\": \"\", \"profilePictureUrl\": \"\", \"content\": \"\"" +
                        " }," +
                        " \"content\": \"$message\"" +
                        " }"
            )
        }
        val res = request<RemoteMessage> {
            response
        }

        res.onSuccess {
            thisLogger().warn(
                "MAHYA:: Create message IS SUCCESS in ✅ ${res.map { it }}"
            )
        }

        return res
    }

    /**
    https://stackoverflow.com/questions/64830813/ktor-response-streaming
    From this link:
    "Some HTTP client libraries only return the response body after the connection has been closed by the server."
    In this method I'm only able to read the buffer after 25 seconds!
     */
    fun getLastAgentMessageId(
        conversationId: String = CONVERSATION_ID,
    ): Flow<MessageMeta> {
        var messageId: String? = null
        val url = "$BASE_URL/$CONVERSATION_PATH/$conversationId/events"

        val eventsConnection = URL(url).openConnection()
            .apply {
                readTimeout = 10000
                connectTimeout = 60000
                setRequestProperty("Authorization", "Bearer $API_KEY")
            }

        val inputStream = eventsConnection.getInputStream()
        val buffer = ByteArray(2048) // 2KB buffer size
        var bytesRead: Int

        val threeChunks: MutableList<String> = MutableList(3) { "" }
        inputStream.use { stream ->
            try {
                while (stream.read(buffer).also { bytesRead = it } != -1) {

                    val chunks = saveChunksAndReturnThem(buffer, bytesRead, threeChunks)

                    val index = chunks.indexOf("\"type\":\"agent_message_new\"")
                    if (index != -1) {
                        val id = chunks
                            .substringAfterLast("\"type\":\"agent_message_new\"")
                            .substringAfter("\"messageId\":")
                            .substringBefore(",")

                        messageId = id.substringAfter("\"").substringBefore("\"")
                        println("MAHYA:: ID $messageId TIME: ${Date.from(Instant.now())} \n")
                    }
                }
            } catch (ex: Exception) {
                println("MAHYA:: Exception $ex")
            }
        }

        println("MAHYA:: DONE : Message ID $messageId  Conversation ID $conversationId  TIME: ${Date.from(Instant.now())}\n")

        return flowOf(
            if (messageId != null) {
                MessageMeta.Available(
                    messageId = messageId!!,
                    conversationId = conversationId
                )
            } else {
                MessageMeta.NotAvailable
            }
        )
    }

    fun getMessageContent(
        conversationId: String,
        messageId: String,
    ): String {
        val messagesPath = "messages/$messageId/events"
        val url = "$BASE_URL/$CONVERSATION_PATH/$conversationId/$messagesPath"

        // Reads all the events and waits until the stream is closed
        val eventsConnection = URL(url).openConnection()
            .apply {
                readTimeout = 10000
                connectTimeout = 60000
                setRequestProperty("Authorization", "Bearer $API_KEY")
            }

        val buffer = ByteArray(2048) // 2KB buffer size
        var bytesRead: Int

        val threeChunks: MutableList<String> = MutableList(3) { "" }

        eventsConnection.getInputStream().use { stream ->
            try {
                while (stream.read(buffer).also { bytesRead = it } != -1) {
                    val chunks = saveChunksAndReturnThem(buffer, bytesRead, threeChunks)
                    val index = chunks.indexOf("\"type\":\"agent_message_success\"")
                    if (index != -1) {
                        val content = chunks
                            .substringAfterLast("\"type\":\"agent_message_success\"")
                            .substringAfter("\"content\":")
                            .substringBefore(",\"chainOfThought\"")

                        println("MAHYA:: CONTENT $content TIME: ${Date.from(Instant.now())} \n")
                    }
                }


            } catch (ex: Exception) {
                println("MAHYA:: Exception $ex")
            }
        }


        return ""
    }

    private fun saveChunksAndReturnThem(
        buffer: ByteArray,
        bytesRead: Int,
        threeChunks: MutableList<String>
    ): String {
        // We need at most two chunks to get the agents message id
        // one is not enough because it can be received at the ned of the chunk so
        // it will be broken
        val chunk = String(buffer.copyOf(bytesRead))
        if (threeChunks[0].isEmpty()) {
            threeChunks[0] = chunk
        } else if (threeChunks[1].isEmpty()) {
            threeChunks[1] = chunk
        } else if (threeChunks[2].isEmpty()) {
            threeChunks[2] = chunk
        } else {
            threeChunks[0] = threeChunks[1]
            threeChunks[1] = threeChunks[2]
            threeChunks[2] = chunk
        }

        return threeChunks.toString()
    }


    companion object {
        private const val ASSISTANTS_PATH = "assistant/agent_configurations"
        private const val CONVERSATION_PATH = "assistant/conversations"
        private val BASE_URL = "https://dust.tt/api/v1/w/$WORKSPACE_ID"
    }
}

sealed interface MessageMeta {
    data object NotAvailable : MessageMeta
    data class Available(
        val messageId: String,
        val conversationId: String
    ) : MessageMeta
}

