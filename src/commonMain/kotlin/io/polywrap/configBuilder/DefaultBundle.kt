package io.polywrap.configBuilder

import io.polywrap.configBuilder.embeds.getIpfsHttpClientWrap
import io.polywrap.configBuilder.embeds.getIpfsResolverWrap
import io.polywrap.core.WrapPackage
import io.polywrap.plugins.filesystem.fileSystemPlugin
import io.polywrap.plugins.http.httpPlugin
import io.polywrap.uriResolvers.extendable.ExtendableUriResolver

class DefaultBundle {

    interface IDefaultEmbed {
        val uri: String
        val pkg: WrapPackage
        val source: String
    }

    interface IDefaultPlugin {
        val uri: String
        val plugin: WrapPackage
        val implements: List<String>
    }

    companion object {
        val ipfsProviders: List<String> = listOf(
            "https://ipfs.wrappers.io",
            "https://ipfs.io"
        )

        val embeds: Map<String, IDefaultEmbed> = mapOf(
            "ipfsHttpClient" to object : IDefaultEmbed {
                override val uri = validateUri("embed/ipfs-http-client@1.0.0")
                override val pkg = getIpfsHttpClientWrap()
                override val source = validateUri("ens/wraps.eth:ipfs-http-client@1.0.0")
            },
            "ipfsResolver" to object : IDefaultEmbed {
                override val uri = validateUri("embed/async-ipfs-uri-resolver-ext@1.0.0")
                override val pkg = getIpfsResolverWrap()
                override val source = validateUri("ens/wraps.eth:async-ipfs-uri-resolver-ext@1.0.0")
            }
        )

        val textRecordResolverRedirect: Pair<String, String> =
            validateUri("ens/wraps.eth:ens-text-record-uri-resolver-ext@1.0.0") to validateUri("ipfs/QmaM318ABUXDhc5eZGGbmDxkb2ZgnbLxigm5TyZcCsh1Kw")

        val uriResolverExts: List<String> = listOf(
            embeds["ipfsResolver"]!!.source,
//            textRecordResolverRedirect.first,
            validateUri("wrap://ipfs/QmbsxmNDbJ3uNp9CRBTfTVQJikCYWQrFcXbC3mVpTRTLZg"), // validateUri("ens/wraps.eth:http-uri-resolver-ext@1.0.0"),
            validateUri("wrap://ipfs/QmQLEc9G4YnMxLexgkWzQo1jHVhfmdLQWX7zXm4Kh9RPMU") // validateUri("ens/wraps.eth:file-system-uri-resolver-ext@1.0.0"),
//            validateUri("ens/wraps.eth:ens-uri-resolver-ext@1.0.0"),
//            validateUri("ens/wraps.eth:ens-ipfs-contenthash-uri-resolver-ext@1.0.0"),
//            validateUri("ens/wraps.eth:ens-ocr-contenthash-uri-resolver-ext@1.0.0")
        )

        val plugins: Map<String, IDefaultPlugin> = mapOf(
            "http" to object : IDefaultPlugin {
                override val uri = validateUri("plugin/http@1.1.0")
                override val plugin = httpPlugin(null)
                override val implements = listOf(
                    validateUri("ens/wraps.eth:http@1.1.0"),
                    validateUri("ens/wraps.eth:http@1.0.0")
                )
            },
            "fileSystem" to object : IDefaultPlugin {
                override val uri = validateUri("plugin/file-system@1.0.0")
                override val plugin = fileSystemPlugin(null)
                override val implements = listOf(
                    validateUri("ens/wraps.eth:file-system@1.0.0")
                )
            }
        )

        /**
         * Get the default Client configuration bundle
         */
        fun getConfig(): BuilderConfig {
            val builder = ConfigBuilder()

            // Add all embedded packages
            for (embed in embeds.values) {
                builder.addPackage(embed.uri to embed.pkg)

                // Add source redirect
                builder.addRedirect(embed.source to embed.uri)

                // Add source implementation
                builder.addInterfaceImplementation(embed.source, embed.uri)
            }

            for (plugin in plugins.values) {
                builder.addPackage(plugin.uri to plugin.plugin)

                // Add all interface implementations & redirects
                for (interfaceUri in plugin.implements) {
                    builder.addInterfaceImplementation(interfaceUri, plugin.uri)
                    builder.addRedirect(interfaceUri to plugin.uri)
                }
            }

            // Add all uri-resolver-ext interface implementations
            builder.addInterfaceImplementations(
                ExtendableUriResolver.defaultExtInterfaceUris[0].uri,
                uriResolverExts.map { it }
            )
            builder.addRedirect(textRecordResolverRedirect.first to textRecordResolverRedirect.second)

            // Configure the ipfs-uri-resolver provider endpoints & retry counts
            builder.addEnv(
                embeds["ipfsResolver"]!!.source to
                    mapOf(
                        "provider" to ipfsProviders[0],
                        "fallbackProviders" to ipfsProviders.slice(1 until ipfsProviders.size),
                        "retries" to mapOf("tryResolveUri" to 2, "getFile" to 2),
                        "timeout" to 10000
                    )
            )

            return builder.config
        }
    }
}
