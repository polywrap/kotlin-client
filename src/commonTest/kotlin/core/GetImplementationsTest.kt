package core

import io.polywrap.core.Uri
import io.polywrap.core.UriResolutionContext
import io.polywrap.core.resolution.*
import io.polywrap.core.resolution.algorithms.getImplementations
import io.polywrap.uriResolvers.embedded.UriRedirect
import kotlin.test.*

class GetImplementationsTest {
    private fun getUriResolutionHandler(redirects: List<UriRedirect>): UriResolutionHandler {
        return object : UriResolutionHandler {
            override fun tryResolveUri(uri: Uri, resolutionContext: UriResolutionContext?, resolveToPackage: Boolean): Result<UriPackageOrWrapper> {
                var currentUri = uri
                val result: UriPackageOrWrapper
                while (true) {
                    val redirect = redirects.find { it.first.uri == currentUri.uri }
                    if (redirect != null) {
                        currentUri = redirect.second
                    } else {
                        result = UriPackageOrWrapper.UriValue(currentUri)
                        break
                    }
                }
                return Result.success(result)
            }
        }
    }

    @Test
    fun worksWithComplexRedirects() {
        val interface1Uri = "wrap://ens/some-interface1.eth"
        val interface2Uri = "wrap://ens/some-interface2.eth"
        val interface3Uri = "wrap://ens/some-interface3.eth"

        val implementation1Uri = "wrap://ens/some-implementation.eth"
        val implementation2Uri = "wrap://ens/some-implementation2.eth"
        val implementation3Uri = "wrap://ens/some-implementation3.eth"

        val redirects = listOf(
            UriRedirect(Uri(interface1Uri), Uri(interface2Uri)),
            UriRedirect(Uri(implementation1Uri), Uri(implementation2Uri)),
            UriRedirect(Uri(implementation2Uri), Uri(implementation3Uri))
        )

        val interfaces = mapOf(
            Uri(interface1Uri) to listOf(Uri(implementation1Uri), Uri(implementation2Uri)),
            Uri(interface2Uri) to listOf(Uri(implementation3Uri)),
            Uri(interface3Uri) to listOf(Uri(implementation3Uri))
        )

        val getImplementationsResult1 = getImplementations(
            Uri(interface1Uri),
            interfaces,
            getUriResolutionHandler(redirects)
        )
        val getImplementationsResult2 = getImplementations(
            Uri(interface2Uri),
            interfaces,
            getUriResolutionHandler(redirects)
        )
        val getImplementationsResult3 = getImplementations(
            Uri(interface3Uri),
            interfaces,
            getUriResolutionHandler(redirects)
        )

        assertEquals(
            Result.success(
                listOf(
                    Uri(implementation1Uri),
                    Uri(implementation2Uri),
                    Uri(implementation3Uri)
                )
            ),
            getImplementationsResult1
        )

        assertEquals(
            Result.success(
                listOf(
                    Uri(implementation1Uri),
                    Uri(implementation2Uri),
                    Uri(implementation3Uri)
                )
            ),
            getImplementationsResult2
        )

        assertEquals(
            Result.success(listOf(Uri(implementation3Uri))),
            getImplementationsResult3
        )
    }

    @Test
    fun interfaceImplementationsAreNotRedirected() {
        val interface1Uri = "wrap://ens/some-interface1.eth"

        val implementation1Uri = "wrap://ens/some-implementation.eth"
        val implementation2Uri = "wrap://ens/some-implementation2.eth"

        val redirects = listOf(
            UriRedirect(Uri(implementation1Uri), Uri(implementation2Uri))
        )

        val interfaces = mapOf(
            Uri(interface1Uri) to listOf(Uri(implementation1Uri))
        )

        val result = getImplementations(
            Uri(interface1Uri),
            interfaces,
            getUriResolutionHandler(redirects)
        )

        assertEquals(result, Result.success(listOf(Uri(implementation1Uri))))
    }
}
