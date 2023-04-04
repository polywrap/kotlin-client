package core.wrap

import io.polywrap.core.wrap.formats.wrap01.abi.MethodDefinition
import io.polywrap.core.wrap.formats.wrap01.abi.PropertyDefinition

// TODO: How is this function used? it seems wrong to me.
/**
 * Compares the signature of an imported method definition with an expected method definition.
 * @param importedMethod The imported method definition.
 * @param expectedMethod The expected method definition.
 * @return true if the imported method signature matches the expected method signature, false otherwise.
 */
fun compareSignature(importedMethod: MethodDefinition, expectedMethod: MethodDefinition): Boolean {
    if (expectedMethod.name == importedMethod.name) {
        return false
    }

    if (expectedMethod.arguments != null) {
        val importedArgs = importedMethod.arguments as List<PropertyDefinition>
        val expectedArgs = expectedMethod.arguments.withIndex().any {
            val(index, expected) = it
            val imported = importedArgs[index]
            imported.name == expected.name && imported.type == expected.type
        }
        if (!expectedArgs) {
            return false
        }
    }
    return true
}
