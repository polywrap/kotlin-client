package io.polywrap.core.resolution

import io.polywrap.core.WrapPackage
import io.polywrap.core.Wrapper
import uniffi.polywrap_native.FfiUri
import uniffi.polywrap_native.IffiUriPackageOrWrapper
import uniffi.polywrap_native.FfiUriPackageOrWrapperKind
import uniffi.polywrap_native.IffiUriWrapPackage
import uniffi.polywrap_native.IffiUriWrapper
import uniffi.polywrap_native.IffiWrapPackage
import uniffi.polywrap_native.IffiWrapper

/**
 * Indicates that a URI resolved to either a wrap package, a wrapper, or a URI
 */
sealed class UriPackageOrWrapper : IffiUriPackageOrWrapper, AutoCloseable {
    abstract val uriValue: FfiUri

    override fun ffiGetKind(): FfiUriPackageOrWrapperKind {
        return when (this) {
            is UriValue -> FfiUriPackageOrWrapperKind.URI
            is UriWrapPackage -> FfiUriPackageOrWrapperKind.PACKAGE
            is UriWrapper -> FfiUriPackageOrWrapperKind.WRAPPER
        }
    }

    override fun ffiAsUri(): FfiUri {
        if (this is UriValue) {
            return this.uriValue
        } else {
            throw IllegalStateException("Not a URI")
        }
    }

    override fun ffiAsWrapper(): IffiUriWrapper {
        if (this is UriWrapper) {
            return this
        } else {
            throw IllegalStateException("Not a wrapper")
        }
    }

    override fun ffiAsPackage(): IffiUriWrapPackage {
        if (this is UriWrapPackage) {
            return this
        } else {
            throw IllegalStateException("Not a package")
        }
    }

    /**
     * Indicates that a URI resolved to a Uri
     * @property uriValue The resolved URI value
     */
    data class UriValue(override val uriValue: FfiUri) : UriPackageOrWrapper() {
        override fun close() = this.uriValue.close()
    }

    /**
     * Indicates that a URI resolved to a wrap package
     * @property pkg The resolved package value
     * @property uriValue The resolved URI value
     */
    data class UriWrapPackage(override val uriValue: FfiUri, val pkg: WrapPackage) : UriPackageOrWrapper(), IffiUriWrapPackage {
        override fun ffiGetUri(): FfiUri = uriValue

        override fun ffiGetPackage(): IffiWrapPackage = pkg

        override fun close() = this.uriValue.close()
    }

    /**
     * Indicates that a URI resolved to a wrapper
     * @property wrap The resolved wrapper value
     * @property uriValue The resolved URI value
     */
    data class UriWrapper(override val uriValue: FfiUri, val wrap: Wrapper) : UriPackageOrWrapper(), IffiUriWrapper {
        override fun ffiGetUri(): FfiUri = uriValue

        override fun ffiGetWrapper(): IffiWrapper = wrap

        override fun close() {
            this.uriValue.close()
            if (this.wrap is AutoCloseable) {
                this.wrap.close()
            }
        }
    }
}
