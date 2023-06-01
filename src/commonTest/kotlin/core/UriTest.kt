package core

import io.polywrap.core.Uri
import io.polywrap.core.resolution.UriConfig
import kotlin.test.*

class UriTest {
    @Test
    fun insertsWrapSchemeWhenNotPresent() {
        val uri = Uri("/authority-v2/path.to.thing.root/sub/path")

        assertEquals("wrap://authority-v2/path.to.thing.root/sub/path", uri.uri)
        assertEquals("authority-v2", uri.authority)
        assertEquals("path.to.thing.root/sub/path", uri.path)
    }

    @Test
    fun failsIfAnAuthorityIsNotPresent() {
        assertFailsWith(IllegalArgumentException::class) {
            Uri("wrap://path")
        }
    }

    @Test
    fun failsIfAPathIsNotPresent() {
        assertFailsWith(IllegalArgumentException::class) {
            Uri("wrap://authority")
        }
    }

    @Test
    fun failsIfSchemeIsNotAtTheBeginning() {
        assertFailsWith(IllegalArgumentException::class) {
            Uri("path/wrap://something")
        }
    }

    @Test
    fun failsWithAnEmptyString() {
        assertFailsWith(IllegalArgumentException::class) {
            Uri("")
        }
    }

    @Test
    fun returnsTrueIfUriIsValid() {
        assertTrue(Uri.isValidUri("wrap://valid/uri"))
    }

    @Test
    fun returnsFalseIfUriIsInvalid() {
        assertFalse(Uri.isValidUri("wrap://....."))
    }

    @Test
    fun returnsParsedUriConfigFromParseUri() {
        val config = Uri.parseUri("wrap://valid/uri")

        assertTrue(config.isSuccess)
        assertEquals(UriConfig("wrap://valid/uri", "valid", "uri"), config.getOrNull())
    }

    @Test
    fun infersCommonUriAuthorities() {
        var uri = Uri("https://domain.com/path/to/thing")
        assertEquals("wrap://https/domain.com/path/to/thing", uri.uri)
        assertEquals("https", uri.authority)
        assertEquals("domain.com/path/to/thing", uri.path)

        uri = Uri("http://domain.com/path/to/thing")
        assertEquals("wrap://http/domain.com/path/to/thing", uri.uri)
        assertEquals("http", uri.authority)
        assertEquals("domain.com/path/to/thing", uri.path)

        uri = Uri("ipfs://QmaM318ABUXDhc5eZGGbmDxkb2ZgnbLxigm5TyZcCsh1Kw")
        assertEquals("wrap://ipfs/QmaM318ABUXDhc5eZGGbmDxkb2ZgnbLxigm5TyZcCsh1Kw", uri.uri)
        assertEquals("ipfs", uri.authority)
        assertEquals("QmaM318ABUXDhc5eZGGbmDxkb2ZgnbLxigm5TyZcCsh1Kw", uri.path)

        uri = Uri("ens://domain.eth")
        assertEquals("wrap://ens/domain.eth", uri.uri)
        assertEquals("ens", uri.authority)
        assertEquals("domain.eth", uri.path)

        uri = Uri("ens://domain.eth:pkg@1.0.0")
        assertEquals("wrap://ens/domain.eth:pkg@1.0.0", uri.uri)
        assertEquals("ens", uri.authority)
        assertEquals("domain.eth:pkg@1.0.0", uri.path)
    }
}
