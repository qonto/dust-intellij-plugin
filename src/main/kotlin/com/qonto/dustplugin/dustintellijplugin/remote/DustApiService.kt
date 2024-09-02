package com.qonto.dustplugin.dustintellijplugin.remote

import API_KEY
import WORKSPACE_ID
import com.intellij.openapi.diagnostic.thisLogger
import com.qonto.dustplugin.dustintellijplugin.remote.models.ConversationInfo
import com.qonto.dustplugin.dustintellijplugin.remote.models.RemoteAssistant
import com.qonto.dustplugin.dustintellijplugin.remote.models.RemoteAssistants
import com.qonto.dustplugin.dustintellijplugin.remote.models.RemoteConversation
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class DustApiService {

    private val httpClient = HttpClient {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        install(Logging) { logger = Logger.SIMPLE }
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
                            it.name
                        }
                    }
                }\n Size of agents: ${it.agentConfigurations.size}"
            )
        }

        return res.map { it.agentConfigurations }
    }

    suspend fun createConversation(): Result<ConversationInfo> {
        val url = "$BASE_URL/$CREATE_CONVERSATION_PATH"

        val response: HttpResponse = httpClient.post(url) {
            headerBuilder()
            setBody("{ \"blocking\": false, \"visibility\": \"unlisted\", \"title\": \"Default\" }")
        }

        val res = request<RemoteConversation> {
            response
        }
        thisLogger().info("MAHYA:: createConversation RESPONSE IS $res")
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

    companion object {
        private const val ASSISTANTS_PATH = "assistant/agent_configurations"
        private const val CREATE_CONVERSATION_PATH = "assistant/conversations"
        private val BASE_URL = "https://dust.tt/api/v1/w/$WORKSPACE_ID"
    }
}