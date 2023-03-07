package eth.krisbitney.polywrap.core.msgpack

import com.ensarsarajcic.kotlinx.serialization.msgpack.exceptions.MsgPackSerializationException
import com.ensarsarajcic.kotlinx.serialization.msgpack.extensions.BaseMsgPackExtensionSerializer
import com.ensarsarajcic.kotlinx.serialization.msgpack.extensions.MsgPackExtension
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.encoding.Encoder

@Serializable(with = MsgPackMapExtensionSerializer::class)
data class MsgPackMap<K, V>(val map: Map<K, V>)

class MsgPackMapExtensionSerializer<K, V>(
    private val keySerializer: KSerializer<K>,
    private val valueSerializer: KSerializer<V>
) : BaseMsgPackExtensionSerializer<MsgPackMap<K, V>>() {

    private val serializer = MsgPackExtension.serializer()
    override val extTypeId: Byte = 1

    // overriding because there is an error in the base class implementation
    override fun serialize(encoder: Encoder, value: MsgPackMap<K, V>) {
        val extension = serialize(value)
        if (extension.extTypeId != extTypeId) {
            throw MsgPackSerializationException.extensionSerializationWrongType(extension, extTypeId, extension.extTypeId)
        }
        encoder.encodeSerializableValue(serializer, extension)
    }

    override fun deserialize(extension: MsgPackExtension): MsgPackMap<K, V> {
        val mapSerializer = MapSerializer(keySerializer, valueSerializer)
        val map = msgPack.decodeFromByteArray(mapSerializer, extension.data)
        return MsgPackMap(map)
    }

    override fun serialize(extension: MsgPackMap<K, V>): MsgPackExtension {
        val mapSerializer = MapSerializer(keySerializer, valueSerializer)
        val data = msgPack.encodeToByteArray(mapSerializer, extension.map)

        val type = if (data.size <= UByte.MAX_VALUE.toInt()) MsgPackExtension.Type.EXT8
        else if (data.size <= Short.MAX_VALUE.toInt()) MsgPackExtension.Type.EXT16
        else MsgPackExtension.Type.EXT32

        return MsgPackExtension(type, extTypeId, data)
    }
}