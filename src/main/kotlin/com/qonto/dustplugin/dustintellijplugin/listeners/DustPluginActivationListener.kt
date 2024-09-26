package com.qonto.dustplugin.dustintellijplugin.listeners

import com.intellij.openapi.application.ApplicationActivationListener
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.IdeFrame
import com.qonto.dustplugin.dustintellijplugin.DustRepository
import com.qonto.dustplugin.dustintellijplugin.remote.DustApiService
import com.qonto.dustplugin.dustintellijplugin.remote.MessageMeta
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

internal class DustPluginActivationListener : ApplicationActivationListener {

    private val dustRepository = DustRepository(DustApiService())

    override fun applicationActivated(ideFrame: IdeFrame) {
        CoroutineScope(Dispatchers.IO).launch {
            dustRepository.listAllAssistants()
        }

        Messages.showInputDialog(
            ideFrame.component,
            "Choose the assistant from the list above:",
            "Assistant Chooser",
            Messages.getQuestionIcon()
        )?.let { assistant ->

            Messages.showInputDialog(
                ideFrame.component,
                "Enter your message:",
                "User Input",
                Messages.getQuestionIcon()

            )?.let { userInput ->

                CoroutineScope(Dispatchers.IO).launch {
                    dustRepository.listAllAssistants()

                    //            dustRepository.createConversation()

                    dustRepository.createMessage(message = userInput, assistantId = assistant)

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
    }
}
