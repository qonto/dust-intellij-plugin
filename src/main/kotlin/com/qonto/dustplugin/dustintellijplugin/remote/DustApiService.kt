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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.net.URL
import java.time.Instant
import java.util.*

class DustApiService {

    private val CONVERSATION_ID = "t6BI91HmgJ"

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(Logging) { logger = Logger.SIMPLE }
        install(HttpTimeout) {
            requestTimeoutMillis = 1000L
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
    suspend fun getLastAgentMessageId(
        conversationId: String = CONVERSATION_ID,
    ): String? {
        var messageId: String? = null
        val url = "$BASE_URL/$CONVERSATION_PATH/$conversationId/events"

        // Reads all the events and waits until the stream is closed
        withContext(Dispatchers.IO) {
            val eventsConnection = URL(url).openConnection()
                .apply {
                    readTimeout = Int.MAX_VALUE
                    connectTimeout = Int.MAX_VALUE
                    setRequestProperty("Authorization", "Bearer $API_KEY")
                }

            val inputStream = eventsConnection.getInputStream()
            val buffer = ByteArray(4096) // 4KB buffer size
            var bytesRead: Int

            val twoChunks: MutableList<String> = MutableList(2) { "" }
            inputStream.use { stream ->
                while (stream.read(buffer).also { bytesRead = it } != -1) {

                    val chunks = saveChunksAndReturnThem(buffer, bytesRead, twoChunks)

                    val index = chunks.indexOf("\"type\":\"agent_message_new\"")
                    if (index != -1) {
                        val id = chunks
                            .substringAfterLast("\"type\":\"agent_message_new\"")
                            .substringAfter("\"messageId\":")
                            .substringBefore(",")

                        messageId = id
                        println("MAHYA:: ID $messageId \n")
                    }
                }
            }
        }

        println("MAHYA:: DONE : Message ID $messageId \n")

        return messageId
    }

    private fun saveChunksAndReturnThem(
        buffer: ByteArray,
        bytesRead: Int,
        twoChunks: MutableList<String>
    ): String {
        // We need at most two chunks to get the agents message id
        // one is not enough because it can be received at the ned of the chunk so
        // it will be broken
        val chunk = String(buffer.copyOf(bytesRead))
        if (twoChunks[0].isEmpty()) {
            twoChunks.add(0, chunk)
        } else if (twoChunks[1].isEmpty()) {
            twoChunks.add(1, chunk)
        } else {
            Collections.rotate(twoChunks, -1)
            twoChunks.add(1, chunk)
        }

        return twoChunks.toString()
    }


    companion object {
        private const val ASSISTANTS_PATH = "assistant/agent_configurations"
        private const val CONVERSATION_PATH = "assistant/conversations"
        private val BASE_URL = "https://dust.tt/api/v1/w/$WORKSPACE_ID"
    }
}