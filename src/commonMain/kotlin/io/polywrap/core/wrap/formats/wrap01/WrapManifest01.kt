package io.polywrap.core.wrap.formats.wrap01

import io.polywrap.core.wrap.formats.wrap01.abi.Abi01
import io.polywrap.msgpack.msgPackDecode
import io.polywrap.msgpack.msgPackEncode
import kotlinx.serialization.Serializable

/**
 * A data class representing a Wrap Manifest, which describes a WRAP package.
 * @property version The version of the WRAP standard used in this package.
 * @property type The type of wrapper package.
 * @property name The name of the wrapper package.
 * @property abi The ABI (Application Binary Interface) for this package.
 */
@Serializable
data class WrapManifest01(
    val version: String,
    val type: String,
    val name: String,
    val abi: Abi01
) {

    init {
        require(version == "0.1.0" || version == "0.1") { "Unsupported WrapManifest version: $version. Expected version: 0.1.0" }
        require(type == "wasm" || type == "interface" || type == "plugin") { "Unsupported WrapManifest type: $type. Supported types: wasm, plugin, interface" }
        require(Regex("""^[a-zA-Z0-9\-_]+$""").matches(name)) { "WrapManifest name contains invalid characters: $name" }
    }

    /**
     * Serializes the manifest to a [ByteArray] in MessagePack format.
     * @return the serialized manifest as a [ByteArray].
     */
    fun serialize(): ByteArray = msgPackEncode(serializer(), this)

    companion object {
        /**
         * Serializes a [manifest] to a [ByteArray] in MessagePack format.
         * @param manifest a [WrapManifest01]
         * @return the serialized manifest as a [ByteArray].
         */
        fun serialize(manifest: WrapManifest01): ByteArray = msgPackEncode(serializer(), manifest)

        /**
         * Deserializes a given [manifest] represented as a [ByteArray] to a [WrapManifest01] object.
         * @param manifest the serialized manifest to deserialize.
         * @return the deserialized [WrapManifest01] object.
         * @throws Error if the given manifest is not a valid WrapManifest or if it's unable to be parsed.
         */
        fun deserialize(manifest: ByteArray): Result<WrapManifest01> {
            val result: Result<WrapManifest01> = msgPackDecode(serializer(), manifest)
            if (result.isFailure) {
                val err = result.exceptionOrNull()
                val message = "Unable to parse WrapManifest: ${err?.message}"
                return Result.failure(IllegalArgumentException(message))
            }
            return result
        }
    }
}
