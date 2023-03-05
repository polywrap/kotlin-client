package eth.krisbitney.polywrap.core.msgpack

interface MsgPackSerializable {
    fun encode(): ByteArray = msgpackEncode(this)
}