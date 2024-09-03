package com.qonto.dustplugin.dustintellijplugin.listeners

import com.intellij.openapi.application.ApplicationActivationListener
import com.intellij.openapi.wm.IdeFrame
import com.qonto.dustplugin.dustintellijplugin.DustRepository
import com.qonto.dustplugin.dustintellijplugin.remote.DustApiService
import kotlinx.coroutines.runBlocking

internal class DustPluginActivationListener : ApplicationActivationListener {

    private val dustRepository = DustRepository(DustApiService())

    override fun applicationActivated(ideFrame: IdeFrame) {

        runBlocking {
            dustRepository.listAllAssistants()

            dustRepository.createConversation()

            dustRepository.createMessage()
        }
    }
}
