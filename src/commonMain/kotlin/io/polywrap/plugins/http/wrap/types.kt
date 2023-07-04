/// NOTE: This is an auto-generated file.
///       All modifications will be overwritten.

package io.polywrap.plugins.http.wrap

import io.polywrap.core.msgpack.GenericMap
import kotlinx.serialization.Serializable

typealias BigInt = String
typealias BigNumber = String
typealias Json = String

/// Env START ///
/// Env END ///

/// Objects START ///
@Serializable
data class Response(
    val status: Int,
    val statusText: String,
    val headers: GenericMap<String, String>? = null,
    val body: String? = null,
)

@Serializable
data class Request(
    val headers: GenericMap<String, String>? = null,
    val urlParams: GenericMap<String, String>? = null,
    val responseType: ResponseType,
    val body: String? = null,
    val formData: List<FormDataEntry>? = null,
    val timeout: UInt? = null,
)

@Serializable
data class FormDataEntry(
    val name: String,
    val value: String? = null,
    val fileName: String? = null,
    val type: String? = null,
)

/// Objects END ///

/// Enums START ///
@Serializable
enum class ResponseType {
    TEXT,
    BINARY,
}

/// Enums END ///

/// Imported Objects START ///
/// Imported Objects END ///

/// Imported Modules START ///
/// Imported Modules END ///
