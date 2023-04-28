package io.polywrap.plugins.filesystem.wrapHardCoded

import kotlinx.serialization.Serializable

typealias Bytes = ByteArray
typealias BigInt = String
typealias BigNumber = String
typealias Json = String

@Serializable
enum class Encoding {
    ASCII,
    UTF8,
    UTF16LE,
    UCS2,
    BASE64,
    BASE64URL,
    LATIN1,
    BINARY,
    HEX
}
