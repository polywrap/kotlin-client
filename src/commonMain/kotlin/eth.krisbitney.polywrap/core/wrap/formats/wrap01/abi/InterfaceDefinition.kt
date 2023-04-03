package eth.krisbitney.polywrap.core.wrap.formats.wrap01.abi

import kotlinx.serialization.Serializable

/**
 * A data class representing an interface definition in a Wrap ABI.
 *
 * @property type The type of the interface definition.
 * @property kind The kind of the interface definition.
 * @property name The name of the interface definition.
 * @property required A boolean indicating whether the interface definition is required.
 * @property uri The URI of the interface definition.
 * @property namespace The namespace of the interface definition.
 * @property nativeType The native type of the interface definition.
 * @property capabilities A list of capability definitions for the interface definition.
 */
@Serializable
data class InterfaceDefinition(
    override val type: String,
    override val kind: Int,
    override val name: String? = null,
    override val required: Boolean? = false,
    override val uri: String,
    override val namespace: String,
    override val nativeType: String,
    val capabilities: CapabilityDefinition? = null
) : IGenericDefinition, ImportedDefinition {

    /**
     * A data class representing a capability definition in an interface definition in a Wrap ABI.
     *
     * @property getImplementations A data class representing the GetImplementations capability.
     */
    @Serializable
    data class CapabilityDefinition(
        val getImplementations: GetImplementations? = null
    ) {

        /**
         * A data class representing the GetImplementations capability.
         *
         * @property enabled A boolean indicating whether GetImplementations is enabled.
         */
        @Serializable
        data class GetImplementations(
            val enabled: Boolean
        )
    }
}
