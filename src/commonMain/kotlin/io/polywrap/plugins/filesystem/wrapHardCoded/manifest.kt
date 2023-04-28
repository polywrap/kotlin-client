package io.polywrap.plugins.filesystem.wrapHardCoded

import io.polywrap.core.wrap.WrapManifest
import io.polywrap.core.wrap.formats.wrap01.abi.Abi01

val manifest = WrapManifest(
    abi = Abi01(),
    name = "mockManifest",
    type = "plugin",
    version = "0.1.0"
)
