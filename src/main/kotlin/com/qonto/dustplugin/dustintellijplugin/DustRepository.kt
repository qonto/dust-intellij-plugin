package com.qonto.dustplugin.dustintellijplugin

import com.qonto.dustplugin.dustintellijplugin.models.Assistant
import com.qonto.dustplugin.dustintellijplugin.models.Conversation
import com.qonto.dustplugin.dustintellijplugin.remote.DustApiService
import com.qonto.dustplugin.dustintellijplugin.remote.models.ConversationInfo
import com.qonto.dustplugin.dustintellijplugin.remote.models.RemoteAssistant
import com.qonto.dustplugin.dustintellijplugin.remote.models.RemoteMessage

class DustRepository(
    private val dustApiService: DustApiService
) {
    suspend fun listAllAssistants(): Result<List<Assistant>> {
        return dustApiService.listAllAssistants()
            .map {
                it.map { remoteAssistant ->
                    remoteAssistant.toAssistant()
                }
            }
    }

    suspend fun createConversation(): Result<Conversation> {
        return dustApiService.createConversation()
            .map {
                it.toConversation()
            }
    }

    suspend fun createMessage(): Result<RemoteMessage> {
        return dustApiService.createMessage()
    }

    private fun RemoteAssistant.toAssistant() = Assistant(
        id = sId,
        name = name
    )

    private fun ConversationInfo.toConversation() = Conversation(
        id = sId,
    )
}