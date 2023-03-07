package eth.krisbitney.polywrap.msgpack

import kotlinx.serialization.*
import com.ensarsarajcic.kotlinx.serialization.msgpack.MsgPack
import com.ensarsarajcic.kotlinx.serialization.msgpack.MsgPackConfiguration


val msgPack: MsgPack by lazy {
    MsgPack(MsgPackConfiguration(
        rawCompatibility = false,
        strictTypes = false,
        strictTypeWriting = false,
        preventOverflows = true,
        ignoreUnknownKeys = false
    ))
}

/**
 * Encodes a given object into a msgpack byte array
 * @param value The object to encode
 * @return The msgpack byte array
 */
inline fun <reified T : Any> msgPackEncode(value: T): ByteArray {
    return msgPack.encodeToByteArray(value)
}

/**
 * Decodes a given msgpack byte array into an object
 * @param bytes The msgpack byte array to decode
 * @return The decoded object
 */
inline fun <reified T : Any> msgPackDecode(bytes: ByteArray): Result<T> {
    return runCatching { msgPack.decodeFromByteArray(bytes) }
}