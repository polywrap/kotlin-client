package eth.krisbitney.polywrap.plugins.http.wrapHardCoded

import eth.krisbitney.polywrap.core.wrap.WrapManifest
import eth.krisbitney.polywrap.core.wrap.formats.wrap01.abi.Abi01

val manifest = WrapManifest(
    abi = Abi01(),
    name = "mockManifest",
    type = "plugin",
    version = "0.1.0"
)
