package com.qonto.dustplugin.dustintellijplugin.remote.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteMessage(
    @SerializedName("message") val message: MessageInfo
)

@Serializable
data class MessageInfo(
    @SerializedName("content") val content: String,
    @SerializedName("context") val context: RemoteContext,
)

@Serializable
data class RemoteContext(
    @SerializedName("username") val username: String,
)

