package com.qonto.dustplugin.dustintellijplugin.remote

import API_KEY
import WORKSPACE_ID
import com.intellij.openapi.diagnostic.thisLogger
import com.qonto.dustplugin.dustintellijplugin.remote.models.RemoteAssistant
import com.qonto.dustplugin.dustintellijplugin.remote.models.RemoteAssistants
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class DustApiService {

    private val httpClient = HttpClient {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        install(Logging) { logger = Logger.SIMPLE }
    }

    private fun HttpRequestBuilder.headerBuilder() {
        header("accept", "application/json")
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
                "MAHYA:: RESPONSE IS SUCCESS in ✅ ${
                    res.map {
                        it.agentConfigurations.map {
                            it.name
                        }

                    }
                }\n Size of agents: ${it.agentConfigurations.size}"
            )
        }

        return res.map { it.agentConfigurations }
    }

    companion object {
        private const val ASSISTANTS_PATH = "assistant/agent_configurations"
        private val BASE_URL = "https://dust.tt/api/v1/w/$WORKSPACE_ID"
    }
}