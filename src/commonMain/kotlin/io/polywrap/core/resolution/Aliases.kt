package io.polywrap.core.resolution

import uniffi.main.FfiExtendableUriResolver
import uniffi.main.FfiRecursiveUriResolver
import uniffi.main.FfiStaticUriResolver
import uniffi.main.FfiUri
import uniffi.main.FfiUriPackageOrWrapperKind
import uniffi.main.FfiUriResolutionContext
import uniffi.main.FfiUriResolutionStep
import uniffi.main.FfiUriResolver

typealias UriPackageOrWrapperKind = FfiUriPackageOrWrapperKind
typealias Uri = FfiUri
typealias UriResolutionContext = FfiUriResolutionContext
typealias UriResolutionStep = FfiUriResolutionStep
typealias UriResolver = FfiUriResolver
typealias StaticUriResolver = FfiStaticUriResolver
typealias ExtendableUriResolver = FfiExtendableUriResolver
typealias RecursiveUriResolver = FfiRecursiveUriResolver