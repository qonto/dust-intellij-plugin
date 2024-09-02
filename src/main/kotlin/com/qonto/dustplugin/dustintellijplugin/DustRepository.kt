package com.qonto.dustplugin.dustintellijplugin

import com.intellij.openapi.diagnostic.thisLogger
import com.qonto.dustplugin.dustintellijplugin.models.Assistant
import com.qonto.dustplugin.dustintellijplugin.remote.DustApiService
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

    private fun RemoteAssistant.toAssistant() = Assistant(
        id = sId,
        name = name
    )
}