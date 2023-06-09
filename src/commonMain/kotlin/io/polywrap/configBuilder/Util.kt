package io.polywrap.configBuilder

import io.polywrap.core.resolution.Uri

fun validateUri(uri: String): String {
    val ffiUri = Uri.fromString(uri)
    val validatedUri = ffiUri.toStringUri()
    ffiUri.close()
    return validatedUri
}
