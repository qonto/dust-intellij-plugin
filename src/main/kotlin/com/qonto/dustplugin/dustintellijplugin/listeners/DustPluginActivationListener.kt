package com.qonto.dustplugin.dustintellijplugin.listeners

import com.intellij.openapi.application.ApplicationActivationListener
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.wm.IdeFrame
import com.qonto.dustplugin.dustintellijplugin.DustRepository
import com.qonto.dustplugin.dustintellijplugin.remote.DustApiService
import kotlinx.coroutines.runBlocking

internal class DustPluginActivationListener : ApplicationActivationListener {

    private val dustRepository = DustRepository(DustApiService())

    override fun applicationActivated(ideFrame: IdeFrame) {

        runBlocking {
            thisLogger().info("MAHYA:: IN SUSPEND START")

            dustRepository.listAllAssistants()
                .onSuccess {
                    thisLogger().info("MAHYA:: Assistant ⚠️ ${it.size}")
                }
                .onFailure {
                    thisLogger().error("MAHYA:: Assistant IN FAILURE ${it.message}")
                }

            dustRepository.createConversation()
                .onSuccess {
                    thisLogger().info("MAHYA:: Conversation ⚠️ Id: ${it.id}")
                }
                .onFailure {
                    thisLogger().error("MAHYA:: Conversation IN FAILURE ${it.message}")
                }

            thisLogger().info("MAHYA:: IN SUSPEND END")
        }
    }
}
