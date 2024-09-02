package com.qonto.dustplugin.dustintellijplugin

import com.intellij.openapi.diagnostic.thisLogger
import com.qonto.dustplugin.dustintellijplugin.models.Assistant
import com.qonto.dustplugin.dustintellijplugin.models.Conversation
import com.qonto.dustplugin.dustintellijplugin.remote.DustApiService
import com.qonto.dustplugin.dustintellijplugin.remote.models.ConversationInfo
import com.qonto.dustplugin.dustintellijplugin.remote.models.RemoteAssistant

class DustRepository(
    private val dustApiService: DustApiService
) {
    suspend fun listAllAssistants(): Result<List<Assistant>> {
        return dustApiService.listAllAssistants()
            .onFailure {
                thisLogger().error("MAHYA:: DustRepository listAllAssistants on error")
            }
            .onSuccess {
                thisLogger().info("MAHYA:: DustRepository listAllAssistants on success")
            }
            .map {
                it.map { remoteAssistant ->
                    remoteAssistant.toAssistant()
                }
            }
    }

    suspend fun createConversation(): Result<Conversation> {
        return dustApiService.createConversation()
            .onFailure {
                thisLogger().error("MAHYA:: DustRepository createConversation on error ${it.message}")
            }
            .onSuccess {
                thisLogger().info("MAHYA:: DustRepository createConversation on success")
            }
            .map {
                it.toConversation()
            }
    }

    private fun RemoteAssistant.toAssistant() = Assistant(
        id = sId,
        name = name
    )

    private fun ConversationInfo.toConversation() = Conversation(
        id = sId,
    )
}