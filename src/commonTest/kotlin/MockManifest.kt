import io.polywrap.core.wrap.WrapManifest
import io.polywrap.core.wrap.formats.wrap01.WrapManifest01
import io.polywrap.core.wrap.formats.wrap01.abi.Abi01
import io.polywrap.core.wrap.formats.wrap01.abi.GenericDefinition
import io.polywrap.core.wrap.formats.wrap01.abi.MethodDefinition
import io.polywrap.core.wrap.formats.wrap01.abi.ModuleDefinition
import io.polywrap.core.wrap.formats.wrap01.abi.ObjectDefinition
import io.polywrap.core.wrap.formats.wrap01.abi.PropertyDefinition
import io.polywrap.core.wrap.formats.wrap01.abi.ScalarDefinition

val mockManifest = WrapManifest(
    abi = Abi01(),
    name = "mockManifest",
    type = "plugin",
    version = "0.1.0"
)

val testManifest = WrapManifest01(
    abi = Abi01(
        objectTypes = listOf(
            ObjectDefinition(
                type = "SampleResult",
                kind = 1,
                properties = listOf(
                    PropertyDefinition(
                        type = "String",
                        name = "value",
                        required = true,
                        kind = 34,
                        scalar = ScalarDefinition(
                            type = "String",
                            name = "value",
                            required = true,
                            kind = 4
                        )
                    )
                ),
                interfaces = listOf()
            )
        ),
        enumTypes = listOf(),
        interfaceTypes = listOf(),
        importedObjectTypes = listOf(),
        importedModuleTypes = listOf(),
        importedEnumTypes = listOf(),
        importedEnvTypes = listOf(),
        moduleType = ModuleDefinition(
            type = "Module",
            kind = 128,
            methods = listOf(
                MethodDefinition(
                    type = "Method",
                    name = "sampleMethod",
                    required = true,
                    kind = 64,
                    arguments = listOf(
                        PropertyDefinition(
                            type = "String",
                            name = "arg",
                            required = true,
                            kind = 34,
                            scalar = ScalarDefinition(
                                type = "String",
                                name = "arg",
                                required = true,
                                kind = 4
                            )
                        )
                    ),
                    _return = PropertyDefinition(
                        type = "SampleResult",
                        name = "sampleMethod",
                        required = true,
                        kind = 34,
                        _object = GenericDefinition(
                            type = "SampleResult",
                            name = "sampleMethod",
                            required = true,
                            kind = 8192
                        )
                    )
                )
            ),
            imports = listOf(),
            interfaces = listOf()
        )
    ),
    name = "template-wasm-as",
    type = "wasm",
    version = "0.1.0"
)

val encodedTestManifest = byteArrayOf(
    -124, -93, 97, 98, 105, -120, -85, 111, 98, 106, 101, 99, 116, 84, 121,
    112, 101, 115, -111, -124, -92, 116, 121, 112, 101, -84, 83, 97, 109, 112, 108, 101, 82, 101, 115, 117, 108, 116, -92, 107,
    105, 110, 100, 1, -86, 112, 114, 111, 112, 101, 114, 116, 105, 101, 115, -111, -123, -92, 116, 121, 112, 101, -90, 83, 116,
    114, 105, 110, 103, -92, 110, 97, 109, 101, -91, 118, 97, 108, 117, 101, -88, 114, 101, 113, 117, 105, 114, 101, 100, -61,
    -92, 107, 105, 110, 100, 34, -90, 115, 99, 97, 108, 97, 114, -124, -92, 116, 121, 112, 101, -90, 83, 116, 114, 105, 110, 103,
    -92, 110, 97, 109, 101, -91, 118, 97, 108, 117, 101, -88, 114, 101, 113, 117, 105, 114, 101, 100, -61, -92, 107, 105, 110,
    100, 4, -86, 105, 110, 116, 101, 114, 102, 97, 99, 101, 115, -112, -87, 101, 110, 117, 109, 84, 121, 112, 101, 115, -112,
    -82, 105, 110, 116, 101, 114, 102, 97, 99, 101, 84, 121, 112, 101, 115, -112, -77, 105, 109, 112, 111, 114, 116, 101, 100, 79,
    98, 106, 101, 99, 116, 84, 121, 112, 101, 115, -112, -77, 105, 109, 112, 111, 114, 116, 101, 100, 77, 111, 100, 117, 108, 101,
    84, 121, 112, 101, 115, -112, -79, 105, 109, 112, 111, 114, 116, 101, 100, 69, 110, 117, 109, 84, 121, 112, 101, 115, -112,
    -80, 105, 109, 112, 111, 114, 116, 101, 100, 69, 110, 118, 84, 121, 112, 101, 115, -112, -86, 109, 111, 100, 117, 108, 101,
    84, 121, 112, 101, -123, -92, 116, 121, 112, 101, -90, 77, 111, 100, 117, 108, 101, -92, 107, 105, 110, 100, -52, -128, -89,
    109, 101, 116, 104, 111, 100, 115, -111, -122, -92, 116, 121, 112, 101, -90, 77, 101, 116, 104, 111, 100, -92, 110, 97, 109,
    101, -84, 115, 97, 109, 112, 108, 101, 77, 101, 116, 104, 111, 100, -88, 114, 101, 113, 117, 105, 114, 101, 100, -61, -92, 107,
    105, 110, 100, 64, -87, 97, 114, 103, 117, 109, 101, 110, 116, 115, -111, -123, -92, 116, 121, 112, 101, -90, 83, 116, 114, 105,
    110, 103, -92, 110, 97, 109, 101, -93, 97, 114, 103, -88, 114, 101, 113, 117, 105, 114, 101, 100, -61, -92, 107, 105, 110, 100,
    34, -90, 115, 99, 97, 108, 97, 114, -124, -92, 116, 121, 112, 101, -90, 83, 116, 114, 105, 110, 103, -92, 110, 97, 109, 101,
    -93, 97, 114, 103, -88, 114, 101, 113, 117, 105, 114, 101, 100, -61, -92, 107, 105, 110, 100, 4, -90, 114, 101, 116, 117, 114,
    110, -123, -92, 116, 121, 112, 101, -84, 83, 97, 109, 112, 108, 101, 82, 101, 115, 117, 108, 116, -92, 110, 97, 109, 101, -84,
    115, 97, 109, 112, 108, 101, 77, 101, 116, 104, 111, 100, -88, 114, 101, 113, 117, 105, 114, 101, 100, -61, -92, 107, 105, 110,
    100, 34, -90, 111, 98, 106, 101, 99, 116, -124, -92, 116, 121, 112, 101, -84, 83, 97, 109, 112, 108, 101, 82, 101, 115, 117, 108,
    116, -92, 110, 97, 109, 101, -84, 115, 97, 109, 112, 108, 101, 77, 101, 116, 104, 111, 100, -88, 114, 101, 113, 117, 105, 114,
    101, 100, -61, -92, 107, 105, 110, 100, -51, 32, 0, -89, 105, 109, 112, 111, 114, 116, 115, -112, -86, 105, 110, 116, 101, 114,
    102, 97, 99, 101, 115, -112, -92, 110, 97, 109, 101, -80, 116, 101, 109, 112, 108, 97, 116, 101, 45, 119, 97, 115, 109, 45, 97,
    115, -92, 116, 121, 112, 101, -92, 119, 97, 115, 109, -89, 118, 101, 114, 115, 105, 111, 110, -91, 48, 46, 49, 46, 48
)

// val simpleMapManifest = WrapManifest(
//    version = "0.1",
//    name = "simple-map-type",
//    type = "wasm",
//    abi = Abi01(
//        version = "0.1",
//        objectTypes = listOf(
//            ObjectDefinition(
//                type = "CustomMap",
//                kind = 1,
//                properties = listOf(
//                    PropertyDefinition(
//                        type = "Map<String, Int>",
//                        name = "map",
//                        map = MapDefinition(
//                            type = "Map<String, Int>",
//                            scalar = ScalarDefinition(name = "map", type = "Int", required = true, kind = 4),
//                            kind = 262146,
//                            name = "map",
//                            key = MapKeyDefinition(name = "map", type = "String", required = true, kind = 4),
//                            value = GenericDefinition(name = "map", type = "Int", required = true, kind = 4),
//                            required = true
//                        ),
//                        required = true,
//                        kind = 34
//                    ),
//                    PropertyDefinition(
//                        type = "Map<String, Map<String, Int>>",
//                        name = "nestedMap",
//                        map = MapDefinition(
//                            type = "Map<String, Map<String, Int>>",
//                            map = MapDefinition(
//                                type = "Map<String, Int>",
//                                name = "nestedMap",
//                                key = MapKeyDefinition(name = "nestedMap", type = "String", required = true, kind = 4),
//                                value = GenericDefinition(name = "nestedMap", type = "Int", required = true, kind = 4),
//                                required = true,
//                                scalar = ScalarDefinition(name = "nestedMap", type = "Int", required = true, kind = 4),
//                                kind = 262146
//                            ),
//                            kind = 262146,
//                            name = "nestedMap",
//                            key = MapKeyDefinition(name = "nestedMap", type = "String", required = true, kind = 4),
//                            value = GenericDefinition(
//                                type = "Map<String, Int>",
//                                name = "nestedMap",
//                                key = MapKeyDefinition(name = "nestedMap", type = "String", required = true, kind = 4),
//                                value = GenericDefinition(name = "nestedMap", type = "Int", required = true, kind = 4),
//                                required = true,
//                                scalar = ScalarDefinition(name = "nestedMap", type = "Int", required = true, kind = 4),
//                                kind = 262146
//                            ),
//                            required = true
//                        ),
//                        required = true,
//                        kind = 34
//                    )
//                )
//            )
//        ),
//        moduleType = ModuleDefinition(
//            type = "Module",
//            kind = 128,
//            methods = listOf(
//                MethodDefinition(
//                    name = "returnCustomMap",
//                    _return = PropertyDefinition(
//                        type = "CustomMap",
//                        name = "returnCustomMap",
//                        required = true,
//                        kind = 34,
//                        _object = GenericDefinition(name = "returnCustomMap", required = true, type = "CustomMap", kind = 8192)
//                    ),
//                    type = "Method",
//                    kind = 64,
//                    required = true,
//                    arguments = listOf(
//                        PropertyDefinition(
//                            type = "CustomMap",
//                            name = "foo",
//                            required = true,
//                            kind = 34,
//                            _object = GenericDefinition(name = "foo", required = true, type = "CustomMap", kind = 8192)
//                        )
//                    )
//                )
//            )
//        )
//    )
// )
