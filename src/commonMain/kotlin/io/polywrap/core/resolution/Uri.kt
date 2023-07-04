package io.polywrap.core.resolution

import uniffi.polywrap_native.FfiUri
import uniffi.polywrap_native.FfiException

/**
 * Uri class for working with wrap URIs.
 *
 * - wrap://ipfs/QmHASH
 * - wrap://fs/directory/file.txt
 * - wrap://http/https://example.com
 * - wrap://ens/sub.domain.eth
 *
 * Breaking down the various parts of the URI, as it applies
 * to [the URI standard](https://tools.ietf.org/html/rfc3986#section-3):
 * - wrap:// - URI Scheme: differentiates Polywrap URIs.
 * - ipfs/ - URI Authority: allows the Polywrap URI resolution algorithm to determine an authoritative URI resolver.
 * - sub.domain.eth - URI Path: tells the Authority where the Wrapper resides.
 *
 * The primary constructor consumes an FfiUri and deallocates its memory.
 *
 * @property authority the authority component of the Uri
 * @property path the path component of the Uri
 * @property uri the string representation of the Uri
 *
 * @constructor Constructs a Uri instance from a wrap URI string.
 * @throws FfiException if the URI string is invalid
 * @param uri a string representation of a wrap URI
 *
 */
class Uri(ffiUri: FfiUri) {

    /** The string representation of the Uri */
    val uri = ffiUri.use { it.toStringUri() }

    /** The authority component of the Uri */
    val authority = uri.substring("wrap://".length).split("/")[0]

    /** The path component of the Uri */
    val path = uri.substring("wrap://".length + authority.length + 1)

    /**
     * Constructs a Uri instance from a wrap URI string.
     *
     * @param uri a string representation of a wrap URI
     *
     * @throws FfiException if the URI string is invalid
     */
    constructor(uri: String) : this(FfiUri.fromString(uri))

    /**
     * Creates an FfiUri instance from this Uri.
     * The caller owns the returned FfiUri and is responsible for deallocating its memory.
     *
     * @returns FfiUri representation of this Uri */
    internal fun toFfi(): FfiUri = FfiUri.fromString(this.uri)

    override fun equals(other: Any?): Boolean = when (other) {
        is Uri -> this.uri == other.uri
        else -> false
    }

    /** @returns Uri string representation */
    override fun toString(): String = this.uri

    override fun hashCode(): Int {
        return uri.hashCode()
    }
}
