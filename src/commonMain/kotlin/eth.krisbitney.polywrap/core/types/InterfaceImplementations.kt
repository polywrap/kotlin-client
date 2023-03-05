package eth.krisbitney.polywrap.core.types

import eth.krisbitney.polywrap.core.resolution.Uri

/**
 * An interface and a list of wrappers that implement the interface.
 * @property interfaceUri Uri of interface
 * @property implementations Uris of implementations
 */
data class InterfaceImplementations(
    val interfaceUri: Uri,
    val implementations: List<Uri>
)