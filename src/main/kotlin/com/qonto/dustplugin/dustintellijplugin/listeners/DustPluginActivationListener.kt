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
            thisLogger().warn("MAHYA:: IN SUSPEND START")

            dustRepository.listAllAssistants()
                .onSuccess {
                    thisLogger().warn("MAHYA:: IN SUCCESS")
                    thisLogger().info("MAHYA:: Assistant ⚠️ ${it.size}")
                }
                .onFailure {
                    thisLogger().warn("MAHYA:: IN FAILURE")
                }

            thisLogger().warn("MAHYA:: IN SUSPEND END")
        }
    }
}
