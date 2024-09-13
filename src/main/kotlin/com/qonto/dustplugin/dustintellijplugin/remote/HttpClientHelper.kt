package com.qonto.dustplugin.dustintellijplugin.remote

import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.HttpResponse

suspend inline fun <reified T> request(
    requester: () -> HttpResponse
): Result<T> = try {
    val httpResponse: HttpResponse = requester()
    val response: T = httpResponse.body()
    Result.success(response)
} catch (exception: ResponseException) {
    exception.printStackTrace()
    Result.failure(exception)
} catch (exception: Throwable) {
    exception.printStackTrace()
    Result.failure(exception)
}
