package io.polywrap.plugins.http.wrapHardCoded

import io.polywrap.msgpack.MsgPackMap
import kotlinx.serialization.Serializable

typealias Bytes = ByteArray
typealias BigInt = String
typealias BigNumber = String
typealias Json = String

@Serializable
data class HttpRequest(
    val headers: MsgPackMap<String, String>? = null,
    val urlParams: MsgPackMap<String, String>? = null,
    val responseType: HttpResponseType,
    val body: String? = null,
    val formData: List<HttpFormDataEntry>? = null,
    val timeout: Int? = null
)

@Serializable
data class HttpFormDataEntry(
    val name: String,
    val value: String? = null,
    val fileName: String? = null,
    val type: String? = null
)

@Serializable
data class HttpResponse(
    val status: Int,
    val statusText: String,
    val headers: MsgPackMap<String, String>? = null,
    val body: String? = null
)

@Serializable
enum class HttpResponseType {
    TEXT,
    BINARY
}
