package eth.krisbitney.polywrap.core.util

import com.ensarsarajcic.kotlinx.serialization.msgpack.MsgPackDynamicSerializer
import eth.krisbitney.polywrap.core.resolution.Uri
import eth.krisbitney.polywrap.core.types.Client
import eth.krisbitney.polywrap.msgpack.msgPackEncode

/**
 * Returns the environment associated with the first URI in the provided history for which the client has a corresponding
 * environment.
 *
 * @param uriHistory the history of URIs to search through
 * @param client the client used to retrieve the environment by URI
 * @return the first environment in the history for which the client has a corresponding environment, or null if none is found
 */
fun getEnvFromUriHistory(uriHistory: List<Uri>, client: Client): ByteArray? {
    for (uri in uriHistory) {
        val env = client.getEnvByUri(uri)

        if (env != null) {
            return msgPackEncode(MsgPackDynamicSerializer, env)
        }
    }

    return null
}
