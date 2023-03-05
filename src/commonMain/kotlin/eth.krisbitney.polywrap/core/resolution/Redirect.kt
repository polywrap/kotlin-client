package eth.krisbitney.polywrap.core.resolution

import eth.krisbitney.polywrap.core.types.WrapPackage
import eth.krisbitney.polywrap.core.types.Wrapper

/**
 * Associates a URI with an embedded wrap package.
 * @property from URI to redirect from
 * @property to URI to redirect to
 */
data class UriRedirect(
    val from: Uri,
    val to: Uri
)

/**
 * Associates a URI with an embedded wrap package
 *
 * @property uri The package's URI
 * @property pkg The wrap package
 */
data class PackageRedirect(
    val uri: Uri,
    val pkg: WrapPackage,
)

/**
 * Associates a URI with an embedded wrapper.
 *
 * @property uri The URI to resolve to the wrapper.
 * @property wrapper A wrapper instance.
 */
data class WrapperRedirect(
    val uri: Uri,
    val wrapper: Wrapper
)

