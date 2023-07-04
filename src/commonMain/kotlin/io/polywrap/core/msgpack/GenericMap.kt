package io.polywrap.core.msgpack

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
@Serializable(with = GenericMapExtensionSerializer::class)
data class GenericMap<K, V>(val map: Map<K, V>)

/**
 * Convenience method to wrap a [Map] in a MsgPack-serializable [GenericMap] instance.
 */
fun <K, V> Map<K, V>.toGenericMap(): GenericMap<K, V> = GenericMap(this)

/**
 * A custom serializer for serializing [GenericMap] instances using the MsgPack format.
 * @param K The key type of the map.
 * @param V The value type of the map.
 * @property keySerializer The serializer for the key type.
 * @property valueSerializer The serializer for the value type.
 */
class GenericMapExtensionSerializer<K, V>(
    private val keySerializer: KSerializer<K>,
    private val valueSerializer: KSerializer<V>
) : BaseMsgPackExtensionSerializer<GenericMap<K, V>>() {

    override val extTypeId: Byte = 1

    /**
     * Deserializes the given [MsgPackExtension] into a [GenericMap] instance.
     * @param extension The [MsgPackExtension] to be deserialized.
     * @return The deserialized [GenericMap] instance.
     */
    override fun deserialize(extension: MsgPackExtension): GenericMap<K, V> {
        val mapSerializer = MapSerializer(keySerializer, valueSerializer)
        val map = msgPack.decodeFromByteArray(mapSerializer, extension.data)
        return GenericMap(map)
    }

    /**
     * Serializes the given [GenericMap] instance into a [MsgPackExtension].
     * @param extension The [GenericMap] instance to be serialized.
     * @return The serialized [MsgPackExtension] instance.
     */
    override fun serialize(extension: GenericMap<K, V>): MsgPackExtension {
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
