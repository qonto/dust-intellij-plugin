package com.qonto.dustplugin.dustintellijplugin.remote.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteAssistants(
    @SerializedName("agentConfigurations") val agentConfigurations: List<RemoteAssistant>
)

@Serializable
data class RemoteAssistant(
    @SerializedName("sId") val sId: String,
    @SerializedName("name") val name: String
)