package com.qonto.dustplugin.dustintellijplugin.remote.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteConversation(
    @SerializedName("conversation") val conversation: ConversationInfo
)

@Serializable
data class ConversationInfo(
    @SerializedName("sId") val sId: String,
)