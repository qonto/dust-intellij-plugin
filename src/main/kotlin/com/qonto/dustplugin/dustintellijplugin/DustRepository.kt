package com.qonto.dustplugin.dustintellijplugin

import com.qonto.dustplugin.dustintellijplugin.models.Assistant
import com.qonto.dustplugin.dustintellijplugin.models.Conversation
import com.qonto.dustplugin.dustintellijplugin.remote.DustApiService
import com.qonto.dustplugin.dustintellijplugin.remote.MessageMeta
import com.qonto.dustplugin.dustintellijplugin.remote.models.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

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

    suspend fun createMessage(
//        conversationId: String,
//        message: String,
//        assistantId: String
    ): Result<RemoteMessage> {
        return dustApiService.createMessage()
    }

    fun getLastAgentMessageId(
//        conversationId: String
    ): Flow<MessageMeta> {
        return dustApiService.getLastAgentMessageId()
    }

    fun getLastMessage(
        conversationId: String,
        messageId: String,
    ): String {
        return dustApiService.getMessageContent(
            conversationId = conversationId,
            messageId = messageId,
        )
    }

    private fun RemoteAssistant.toAssistant() = Assistant(
        id = sId,
        name = name
    )

    private fun ConversationInfo.toConversation() = Conversation(
        id = sId,
    )
}