package io.polywrap.msgpack

import com.ensarsarajcic.kotlinx.serialization.msgpack.extensions.BaseMsgPackExtensionSerializer
import com.ensarsarajcic.kotlinx.serialization.msgpack.extensions.MsgPackExtension
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer

/**
 * A wrapper data class for a Map to be serialized with MsgPack format.
 * Without this wrapper, a map will be serialized as an object.
 * This wrapper will serialize the map as a Generic Map extension type.
 *
 * @param K The key type of the map.
 * @param V The value type of the map.
 * @property map The map to be serialized.
 */
@Serializable(with = MsgPackMapExtensionSerializer::class)
data class MsgPackMap<K, V>(val map: Map<K, V>)

/**
 * Convenience method to wrap a [Map] in a [MsgPackMap] instance.
 */
fun <K, V> Map<K, V>.toMsgPackMap(): MsgPackMap<K, V> = MsgPackMap(this)

/**
 * A custom serializer for serializing [MsgPackMap] instances using the MsgPack format.
 * @param K The key type of the map.
 * @param V The value type of the map.
 * @property keySerializer The serializer for the key type.
 * @property valueSerializer The serializer for the value type.
 */
class MsgPackMapExtensionSerializer<K, V>(
    private val keySerializer: KSerializer<K>,
    private val valueSerializer: KSerializer<V>
) : BaseMsgPackExtensionSerializer<MsgPackMap<K, V>>() {

    override val extTypeId: Byte = 1

    /**
     * Deserializes the given [MsgPackExtension] into a [MsgPackMap] instance.
     * @param extension The [MsgPackExtension] to be deserialized.
     * @return The deserialized [MsgPackMap] instance.
     */
    override fun deserialize(extension: MsgPackExtension): MsgPackMap<K, V> {
        val mapSerializer = MapSerializer(keySerializer, valueSerializer)
        val map = msgPack.decodeFromByteArray(mapSerializer, extension.data)
        return MsgPackMap(map)
    }

    /**
     * Serializes the given [MsgPackMap] instance into a [MsgPackExtension].
     * @param extension The [MsgPackMap] instance to be serialized.
     * @return The serialized [MsgPackExtension] instance.
     */
    override fun serialize(extension: MsgPackMap<K, V>): MsgPackExtension {
        val mapSerializer = MapSerializer(keySerializer, valueSerializer)
        val data = msgPack.encodeToByteArray(mapSerializer, extension.map)

        val type = if (data.size <= UByte.MAX_VALUE.toInt()) {
            MsgPackExtension.Type.EXT8
        } else if (data.size <= Short.MAX_VALUE.toInt()) {
            MsgPackExtension.Type.EXT16
        } else {
            MsgPackExtension.Type.EXT32
        }

        return MsgPackExtension(type, extTypeId, data)
    }
}
