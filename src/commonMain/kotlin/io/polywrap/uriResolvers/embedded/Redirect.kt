package io.polywrap.uriResolvers.embedded

import io.polywrap.core.resolution.Uri
import io.polywrap.core.types.WrapPackage
import io.polywrap.core.types.Wrapper

/** Associates a URI with a URI to redirect to. */
typealias UriRedirect = Pair<Uri, Uri>

/** Associates a URI with an embedded wrap package. */
typealias PackageRedirect = Pair<Uri, WrapPackage>

/** Associates a URI with an embedded wrapper. */
typealias WrapperRedirect = Pair<Uri, Wrapper>
