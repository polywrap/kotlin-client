package types

import resolution.Uri

/**
 * An interface and a list of wrappers that implement the interface.
 * @property interface Uri of interface
 * @property implementations Uris of implementations
 */
data class InterfaceImplementations(
    val interfaceUri: Uri,
    val implementations: List<Uri>
)