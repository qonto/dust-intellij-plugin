package com.qonto.dustplugin.dustintellijplugin.listeners

import com.intellij.openapi.application.ApplicationActivationListener
import com.intellij.openapi.wm.IdeFrame
import com.qonto.dustplugin.dustintellijplugin.DustRepository
import com.qonto.dustplugin.dustintellijplugin.remote.DustApiService
import com.qonto.dustplugin.dustintellijplugin.remote.MessageMeta
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

internal class DustPluginActivationListener : ApplicationActivationListener {

    override fun applicationActivated(ideFrame: IdeFrame) {
        val dustRepository = DustRepository(DustApiService())

        runBlocking {
            dustRepository.listAllAssistants()
//
//            dustRepository.createConversation()
//
            dustRepository.createMessage()
        }

        CoroutineScope(Dispatchers.IO).launch {
            dustRepository.getLastAgentMessageId().collectLatest {
                when (it) {
                    is MessageMeta.Available -> dustRepository.getLastMessage(
                        conversationId = it.conversationId,
                        messageId = it.messageId,
                    )

                    MessageMeta.NotAvailable -> println("No message available")
                }
            }
        }
    }

}
