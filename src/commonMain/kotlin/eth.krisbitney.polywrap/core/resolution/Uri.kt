package eth.krisbitney.polywrap.core.resolution

/**
 * URI configuration.
 *
 * @property uri Full string representation of URI.
 * @property authority URI Authority: allows the Polywrap URI resolution algorithm to determine an authoritative URI resolver.
 * @property path URI Path: tells the Authority where the Wrapper resides.
 */
data class UriConfig(
    val uri: String,
    val authority: String,
    val path: String,
)

/**
 * Uri class for working with wrap URIs.
 *
 * wrap://ipfs/QmHASH
 * wrap://ens/sub.dimain.eth
 * wrap://fs/directory/file.txt
 * wrap://uns/domain.crypto
 *
 * Breaking down the various parts of the URI, as it applies
 * to [the URI standard](https://tools.ietf.org/html/rfc3986#section-3):
 * wrap:// - URI Scheme: differentiates Polywrap URIs.
 * ipfs/ - URI Authority: allows the Polywrap URI resolution algorithm to determine an authoritative URI resolver.
 * sub.domain.eth - URI Path: tells the Authority where the Wrapper resides.
 *
 * @property authority the authority component of the Uri
 * @property path the path component of the Uri
 * @property uri the string representation of the Uri
 */
class Uri(private val _config: UriConfig) {

    val authority: String
        /** Returns the authority component of the Uri. */
        get() = _config.authority

    val path: String
        /** Returns the path component of the Uri. */
        get() = _config.path

    val uri: String
        /** Returns the string representation of the Uri. */
        get() = _config.uri

    /**
     * Constructs a Uri instance from a wrap URI string.
     *
     * @throws IllegalArgumentException if the URI string is invalid
     *
     * @param uri a string representation of a wrap URI
     */
    constructor(uri: String) : this(parseUri(uri).getOrElse { throw it })

    override fun equals(other: Any?): Boolean = when (other) {
        is Uri -> this.uri == other.uri
        else -> false
    }

    /** @returns Uri string representation */
    override fun toString(): String = this._config.uri

    override fun hashCode(): Int = _config.hashCode()

    companion object {
        /**
         * Tests two Uri instances for equality.
         *
         * @param a the first Uri to compare
         * @param b the second Uri to compare
         * @return true if the two Uris are equal, false otherwise
         */
        fun equals(a: Uri, b: Uri): Boolean = a.uri == b.uri

        /**
         * Tests if a URI string is a valid wrap URI.
         *
         * @param uri the URI string to test
         * @return true if the input string is a valid wrap URI, false otherwise
         */
        fun isValidUri(uri: String): Boolean = parseUri(uri).isSuccess

        /**
         * Parse a wrap URI string into its authority and path
         *
         * @param uri - a string representation of a wrap URI
         * @return A Result containing a UriConfig, if successful, or an error
         */
        fun parseUri(uri: String): Result<UriConfig> {
            if (uri.isEmpty()) {
                return Result.failure(IllegalArgumentException("The provided URI is empty"))
            }

            var processed = uri

            // Trim preceding '/' characters
            while (processed[0] == '/') {
                processed = processed.substring(1)
            }

            // Check for the wrap:// scheme, add if it isn't there
            val wrapSchemeIdx = processed.indexOf("wrap://")

            // If it's missing the wrap:// scheme, add it
            if (wrapSchemeIdx == -1) {
                processed = "wrap://$processed"
            }

            // If the wrap:// is not in the beginning, return an error
            if (wrapSchemeIdx > -1 && wrapSchemeIdx != 0) {
                return Result.failure(IllegalArgumentException("The wrap:// scheme must be at the beginning of the URI string"))
            }

            // Extract the authority & path
            val re = """^wrap://((?<authority>[a-z][a-z0-9-_]+)/)?(?<path>.*)$""".toRegex()
            val result = re.matchEntire(processed)?.groups as? MatchNamedGroupCollection ?: return Result.failure(getErr(uri))

            var path = result["path"]?.value ?: return Result.failure(getErr(uri))
            var authority = result["authority"]?.value

            if (authority == null) {
                val inferred = inferAuthority(path) ?: return Result.failure(getErr(uri))
                authority = inferred.authority
                path = inferred.path
                processed = "wrap://$authority/$path"
            }

            return Result.success(
                UriConfig(
                    uri = processed,
                    authority = authority,
                    path = path
                )
            )
        }

        private fun inferAuthority(_path: String): UriConfig? {
            val re = """^(?<authority>[a-z][a-z0-9-_]+)://(?<path>.*)$""".toRegex()
            val result = re.matchEntire(_path)?.groups as? MatchNamedGroupCollection ?: return null

            val authority = result["authority"]?.value ?: return null
            val path = result["path"]?.value ?: return null

            return UriConfig("wrap://$authority/$path", authority, path)
        }

        private fun getErr(uri: String): IllegalArgumentException {
            return IllegalArgumentException(
                "URI is malformed, here are some examples of valid URIs:\n" +
                        "wrap://ipfs/QmHASH\n" +
                        "wrap://ens/domain.eth\n" +
                        "ens/domain.eth\n\n" +
                        "Invalid URI Received: $uri"
            )
        }
    }
}

