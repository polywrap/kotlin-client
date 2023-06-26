package io.polywrap.core.msgpack

import com.ensarsarajcic.kotlinx.serialization.msgpack.MsgPack
import com.ensarsarajcic.kotlinx.serialization.msgpack.MsgPackConfiguration
import com.ensarsarajcic.kotlinx.serialization.msgpack.MsgPackNullableDynamicSerializer
import kotlinx.serialization.*
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer

/**
 * A lazily initialized MsgPack instance with a custom configuration.
 */
val msgPack: MsgPack by lazy {
    MsgPack(
        MsgPackConfiguration(
            rawCompatibility = false,
            strictTypes = false,
            strictTypeWriting = false,
            preventOverflows = true,
            ignoreUnknownKeys = true,
            ordinalEnums = true
        )
    )
}

val NullableKVSerializer = MapSerializer(String.serializer()::nullable.get(), MsgPackNullableDynamicSerializer)::nullable.get()

/**
 * Encodes a given object into a msgpack byte array using the reified type's serializer.
 *
 * @param T the type of the object to encode
 * @param value the object to encode
 * @return the msgpack byte array
 */
inline fun <reified T : Any?> msgPackEncode(value: T): ByteArray {
    return msgPack.encodeToByteArray(serializer(), value)
}

/**
 * Encodes a given object into a msgpack byte array using the provided serializer.
 *
 * @param serializer the serializer to use for encoding the object
 * @param value the object to encode
 * @return the msgpack byte array
 */
fun <T : Any?> msgPackEncode(serializer: SerializationStrategy<T>, value: T): ByteArray {
    return msgPack.encodeToByteArray(serializer, value)
}

/**
 * Decodes a given msgpack byte array into an object using the reified type's deserializer.
 *
 * @param T the type of the object to decode
 * @param bytes the msgpack byte array to decode
 * @return a Result containing the decoded object, or an exception if the decoding fails
 */
inline fun <reified T : Any?> msgPackDecode(bytes: ByteArray): Result<T> {
    return runCatching { msgPack.decodeFromByteArray(serializer(), bytes) }
}

/**
 * Decodes a given msgpack byte array into an object using the provided deserializer.
 *
 * @param serializer the deserializer to use for decoding the object
 * @param bytes the msgpack byte array to decode
 * @return a Result containing the decoded object, or an exception if the decoding fails
 */
fun <T : Any?> msgPackDecode(serializer: DeserializationStrategy<T>, bytes: ByteArray): Result<T> {
    return runCatching { msgPack.decodeFromByteArray(serializer, bytes) }
}
