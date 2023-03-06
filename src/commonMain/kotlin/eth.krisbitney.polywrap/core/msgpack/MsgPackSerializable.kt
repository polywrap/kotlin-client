package eth.krisbitney.polywrap.core.msgpack

/**
 * Interface for objects that can be encoded to MsgPack.
 */
interface MsgPackSerializable {
    /**
     * Encode the object to MsgPack.
     * @return The MsgPack encoded object.
     */
    fun encode(): ByteArray = msgpackEncode(this)
}

/**
 * Encodes this [Map] to a [ByteArray] using the MessagePack serialization format.
 * @return the encoded [ByteArray].
 */
fun Map<*, *>.encodeObject(): ByteArray = msgpackEncode(this)

// TODO: encode as map