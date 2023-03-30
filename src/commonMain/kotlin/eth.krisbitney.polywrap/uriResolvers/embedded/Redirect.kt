package eth.krisbitney.polywrap.uriResolvers.embedded

import eth.krisbitney.polywrap.core.resolution.Uri
import eth.krisbitney.polywrap.core.types.WrapPackage
import eth.krisbitney.polywrap.core.types.Wrapper

/** Associates a URI with a URI to redirect to. */
typealias UriRedirect = Pair<Uri, Uri>

/** Associates a URI with an embedded wrap package. */
typealias PackageRedirect = Pair<Uri, WrapPackage>

/** Associates a URI with an embedded wrapper. */
typealias WrapperRedirect = Pair<Uri, Wrapper>