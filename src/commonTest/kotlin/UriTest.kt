import resolution.Uri
import resolution.UriConfig
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
}