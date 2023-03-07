package eth.krisbitney.polywrap.core.wrap

import com.ensarsarajcic.kotlinx.serialization.msgpack.MsgPack
import com.ensarsarajcic.kotlinx.serialization.msgpack.MsgPackConfiguration
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray

class WrapManifestSerializer {

    private val manifestMsgPack: MsgPack = MsgPack(
            MsgPackConfiguration(
                rawCompatibility = false,
                strictTypes = false,
                strictTypeWriting = false,
                preventOverflows = true,
                ignoreUnknownKeys = false
            ),
        )

    /** Serializes a given [manifest] to a [ByteArray] in MessagePack format.
     * @param manifest the manifest to serialize.
     * @return the serialized manifest as a [ByteArray].
     */
    fun serialize(manifest: WrapManifest): ByteArray {
        return manifestMsgPack.encodeToByteArray(manifest)
    }

    /**
     * Deserializes a given [manifest] represented as a [ByteArray] to a [WrapManifest] object.
     * @param manifest the serialized manifest to deserialize.
     * @return the deserialized [WrapManifest] object.
     * @throws Error if the given manifest is not a valid WrapManifest or if it's unable to be parsed.
     */
    fun deserialize(manifest: ByteArray): Result<WrapManifest> {
        val wrapManifest: WrapManifest
        try {
            wrapManifest = manifestMsgPack.decodeFromByteArray(manifest)
        } catch (e: Exception) {
            val message = "Unable to parse WrapManifest: ${e.message}"
            return Result.failure(IllegalArgumentException(message))
        }
        return Result.success(wrapManifest)
    }
}