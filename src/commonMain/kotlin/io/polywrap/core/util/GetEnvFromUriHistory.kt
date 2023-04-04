package io.polywrap.core.util

import io.polywrap.core.resolution.Uri
import io.polywrap.core.types.Client
import io.polywrap.msgpack.EnvSerializer
import io.polywrap.msgpack.msgPackEncode

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
            return msgPackEncode(EnvSerializer, env)
        }
    }

    return null
}
