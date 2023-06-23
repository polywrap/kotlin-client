import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.`maven-publish`
import org.gradle.kotlin.dsl.signing
import java.util.*

plugins {
    `maven-publish`
    signing
}

// Stub secrets to let the project sync and build without the publication values set up
ext["signing.keyId"] = null
ext["signing.password"] = null
ext["signing.key"] = null
ext["ossrhUsername"] = null
ext["ossrhPassword"] = null

// Grabbing secrets from local.properties file or from environment variables, which could be used on CI
val secretPropsFile = project.rootProject.file("local.properties")
if (secretPropsFile.exists()) {
    secretPropsFile.reader().use {
        Properties().apply {
            load(it)
        }
    }.onEach { (name, value) ->
        ext[name.toString()] = value
    }
} else {
    ext["signing.keyId"] = System.getenv("SIGNING_KEY_ID")
    ext["signing.password"] = System.getenv("SIGNING_PASSWORD")
    ext["signing.key"] = System.getenv("SIGNING_KEY")
    ext["ossrhUsername"] = System.getenv("OSSRH_USERNAME")
    ext["ossrhPassword"] = System.getenv("OSSRH_PASSWORD")
}

fun getExtraString(name: String) = ext[name]?.toString()

afterEvaluate {
    val javadocJar: TaskProvider<Jar> = rootProject.tasks.named("dokkaJavadocJar", Jar::class.java)

    publishing {
        // Configure maven central repository
        repositories {
            maven {
                name = "sonatype"
                if (project.version.toString().endsWith("SNAPSHOT")) {
                    setUrl("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                } else {
                    setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                }
                credentials {
                    username = getExtraString("ossrhUsername")
                    password = getExtraString("ossrhPassword")
                }
            }
        }

        // Configure all publications
        publications.withType<MavenPublication> {
            // Stub javadoc.jar artifact
            artifact(javadocJar.get())

            // Provide artifacts information requited by Maven Central
            pom {
                name.set("polywrap")
                description.set("Polywrap Client for JVM and Android")
                url.set("https://github.com/polywrap/kotlin-client")

                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("krisbitney")
                        name.set("Kristofer Bitney")
                        email.set("kris@polywrap.io")
                    }
                }
                scm {
                    connection.set("scm:git:github.com/polywrap/kotlin-client.git")
                    developerConnection.set("scm:git:ssh://github.com/polywrap/kotlin-client.git")
                    url.set("https://github.com/polywrap/kotlin-client.git")
                }
            }
        }
    }
}

// Signing artifacts. Signing.* extra properties values will be used
signing {
    useInMemoryPgpKeys(
        getExtraString("signing.keyId"),
        getExtraString("signing.key"),
        getExtraString("signing.password"),
    )
    sign(publishing.publications)
}

tasks.withType<PublishToMavenRepository> {
    dependsOn("signJvmPublication", "signKotlinMultiplatformPublication")
}