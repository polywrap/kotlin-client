package core

import eth.krisbitney.polywrap.core.resolution.*
import eth.krisbitney.polywrap.core.types.InterfaceImplementations
import eth.krisbitney.polywrap.core.resolution.algorithms.getImplementations
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.test.*
import kotlinx.coroutines.test.*

class GetImplementationsTest {
    private fun getUriResolutionHandler(redirects: List<UriRedirect>): UriResolutionHandler {
        return object : UriResolutionHandler {
            override suspend fun tryResolveUri(uri: Uri, resolutionContext: UriResolutionContext?): Deferred<Result<UriPackageOrWrapper>> = coroutineScope {
                async {
                    var currentUri = uri
                    val result: UriPackageOrWrapper
                    while (true) {
                        val redirect = redirects.find { it.from.uri == currentUri.uri }
                        if (redirect != null) {
                            currentUri = redirect.to
                        } else {
                            result = UriPackageOrWrapper.UriValue(currentUri)
                            break
                        }
                    }
                    Result.success(result)
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun worksWithComplexRedirects() = runTest {
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

        val interfaces = listOf(
            InterfaceImplementations(
                Uri(interface1Uri),
                listOf(Uri(implementation1Uri), Uri(implementation2Uri))
            ),
            InterfaceImplementations(
                Uri(interface2Uri),
                listOf(Uri(implementation3Uri))
            ),
            InterfaceImplementations(
                Uri(interface3Uri),
                listOf(Uri(implementation3Uri))
            )
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun interfaceImplementationsAreNotRedirected() = runTest {
        val interface1Uri = "wrap://ens/some-interface1.eth"

        val implementation1Uri = "wrap://ens/some-implementation.eth"
        val implementation2Uri = "wrap://ens/some-implementation2.eth"

        val redirects = listOf(
            UriRedirect(Uri(implementation1Uri), Uri(implementation2Uri))
        )

        val interfaces = listOf(
            InterfaceImplementations(
                Uri(interface1Uri),
                listOf(Uri(implementation1Uri))
            )
        )

        val result = getImplementations(
            Uri(interface1Uri),
            interfaces,
            getUriResolutionHandler(redirects)
        )

        assertEquals(result, Result.success(listOf(Uri(implementation1Uri))))
    }
}