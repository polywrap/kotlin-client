package eth.krisbitney.polywrap.core.wrap.formats.wrap01.abi

import kotlinx.serialization.Serializable

/**
 * Abi defines a collection of types that describe the data structures used in a Wrapper.
 * @property objectTypes List of [ObjectDefinition]s that define objects in the ABI.
 * @property moduleType [ModuleDefinition] that defines the module in the ABI.
 * @property enumTypes List of [EnumDefinition]s that define the enums in the ABI.
 * @property interfaceTypes List of [InterfaceDefinition]s that define the interfaces in the ABI.
 * @property importedObjectTypes List of [ImportedObjectDefinition]s that define imported objects in the ABI.
 * @property importedModuleTypes List of [ImportedModuleDefinition]s that define imported modules in the ABI.
 * @property importedEnumTypes List of [ImportedEnumDefinition]s that define imported enums in the ABI.
 * @property importedEnvTypes List of [ImportedEnvDefinition]s that define imported environment variables in the ABI.
 * @property envType [EnvDefinition] that defines environment variables in the ABI.
 * */
@Serializable
data class Abi01(
    val version: String = "0.1",
    val objectTypes: List<ObjectDefinition>? = null,
    val moduleType: ModuleDefinition? = null,
    val enumTypes: List<EnumDefinition>? = null,
    val interfaceTypes: List<InterfaceDefinition>? = null,
    val importedObjectTypes: List<ImportedObjectDefinition>? = null,
    val importedModuleTypes: List<ImportedModuleDefinition>? = null,
    val importedEnumTypes: List<ImportedEnumDefinition>? = null,
    val importedEnvTypes: List<ImportedEnvDefinition>? = null,
    val envType: EnvDefinition? = null
) {
    init {
        require(version == "0.1") { "Unsupported Abi version: $version. Expected version: 0.1" }
    }
}
