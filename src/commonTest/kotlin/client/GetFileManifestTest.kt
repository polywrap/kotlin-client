package client

import io.polywrap.client.PolywrapClient
import io.polywrap.configBuilder.ClientConfigBuilder
import io.polywrap.core.resolution.Uri
import io.polywrap.core.resolution.UriPackageOrWrapper
import io.polywrap.core.wrap.WrapManifest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GetFileManifestTest {

    private val sha3Uri = Uri("ipfs/QmThRxFfr7Hj9Mq6WmcGXjkRrgqMG3oD93SLX27tinQWy5")
    private val config = ClientConfigBuilder().addDefaults().build()
    private val client = PolywrapClient(config)

    @Test
    fun tryResolveUriToPackage() {
        val result = client.tryResolveUri(uri = sha3Uri, resolveToPackage = true)
        assertNull(result.exceptionOrNull())
        assertTrue(result.getOrThrow() is UriPackageOrWrapper.PackageValue)
    }

    @Test
    fun getFile() {
        val result = client.getFile(sha3Uri, "wrap.info")
        assertNull(result.exceptionOrNull())

        val manifest = WrapManifest.deserialize(result.getOrThrow())
        assertNull(manifest.exceptionOrNull())

        assertEquals(manifest.getOrThrow().name, "sha3-wasm-rs")
    }

    @Test
    fun getManifest() {
        val manifest = client.getManifest(sha3Uri)
        assertNull(manifest.exceptionOrNull())
        assertEquals(manifest.getOrThrow().name, "sha3-wasm-rs")
    }
}