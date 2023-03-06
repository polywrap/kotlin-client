package eth.krisbitney.polywrap.core.wrap

import com.ensarsarajcic.kotlinx.serialization.msgpack.MsgPack
import com.ensarsarajcic.kotlinx.serialization.msgpack.MsgPackConfiguration

val manifestMsgPack: MsgPack by lazy {
    MsgPack(
        MsgPackConfiguration(
            rawCompatibility = false,
            strictTypes = false,
            strictTypeWriting = true,
            preventOverflows = true,
            ignoreUnknownKeys = false
        ),
    )
}
