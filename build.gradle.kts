import com.android.build.gradle.internal.tasks.factory.dependsOn

plugins {
    kotlin("multiplatform") version "1.8.20"
    kotlin("plugin.serialization") version "1.8.20"
    id("com.goncalossilva.resources") version "0.2.5"
    id("org.jlleitschuh.gradle.ktlint") version "11.3.1"
    id("com.android.library") version "8.2"
    id("org.jetbrains.dokka") version "1.8.10"
    id("uniffi-pipeline")
    id("convention.publication")
}

group = "io.polywrap"
version = "0.10.0-SNAPSHOT"

repositories {
    mavenLocal()
    google()
    mavenCentral()
}

// Can I set this in a more elegant way?
val uniffiBindingsDir = "$buildDir/generated/source/uniffi/kotlin"

kotlin {
    jvm {
        jvmToolchain(17)
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    android()

    sourceSets {
        val commonMain by getting {
            dependencies {
                kotlin.srcDirs(uniffiBindingsDir)
                implementation("com.ensarsarajcic.kotlinx:serialization-msgpack:0.5.5")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                implementation("com.squareup.okio:okio:3.3.0") // fs plugin
                implementation("io.ktor:ktor-client-core:2.3.1") // http plugin
                implementation("io.ktor:ktor-client-android:2.3.1") // http plugin
                implementation("org.slf4j:slf4j-nop:1.7.36") // suppress SLF4J logger warnings
            }
        }
        val commonTest by getting {
            resources.srcDirs("src/commonMain/resources")
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
                implementation("com.goncalossilva:resources:0.3.2") // access resources in tests
                implementation("io.ktor:ktor-client-mock:2.3.1") // http plugin test
                implementation("com.ionspin.kotlin:bignum:0.3.8") // client test
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1") // client test
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("net.java.dev.jna:jna:5.13.0")
            }
        }
        val jvmTest by getting
        val androidMain by getting {
            dependencies {
                implementation("net.java.dev.jna:jna:5.13.0@aar")
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation("junit:junit:4.13.2")
            }
        }
        val androidInstrumentedTest by getting {
            dependencies {
                implementation("junit:junit:4.13.2")
            }
        }
    }
}

uniffi {
    rustClientRepoBranch = "main"
    clonesDir = "$projectDir/clones"
    desktopJnaPath = "$projectDir/src/jvmMain/resources"
    androidJnaPath = "$projectDir/src/androidMain/resources"
    rustTargets = listOf(
        "armv7-linux-androideabi",
//        "i686-linux-android", // This arch is not used anymore
        "aarch64-linux-android",
//        "x86_64-linux-android", // This arch is not used anymore
//        "x86_64-pc-windows-gnu", // TODO: this is failing
        "aarch64-unknown-linux-gnu",
        "x86_64-unknown-linux-gnu",
        "x86_64-apple-darwin",
        "aarch64-apple-darwin"
    )
    packageName = "polywrap_native"
    libname = "uniffi_polywrap_native"
    bindingsDir = uniffiBindingsDir
    // when false, only builds for current desktop platform even if it's not in the list
    isRelease = false
}

android {
    namespace = "io.polywrap"
    compileSdk = 32
    compileOptions {
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// javadoc generation for Maven repository publication
tasks.register<Jar>("dokkaJavadocJar") {
    dependsOn(tasks.dokkaJavadoc)
    from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

// generate dokka html site and copy it to docs folder
tasks.register<Copy>("copyDokkaHtml") {
    dependsOn(tasks.dokkaHtml)
    val docsDir = "$projectDir/docs"
    doFirst { delete(docsDir) }
    from("$buildDir/dokka/html")
    into(docsDir)
}
// automatically generate docs site when publishing
if (!version.toString().endsWith("-SNAPSHOT")) {
    tasks.publish.dependsOn("copyDokkaHtml")
}

// print stdout during tests
tasks.withType<Test> {
    this.testLogging {
        this.showStandardStreams = true
    }
}

// lint configuration
configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    // this rule is not getting picked up in .editorconfig for some reason
    disabledRules.set(setOf("no-wildcard-imports"))
    filter {
        exclude("**/build/**")
        exclude("**/generated/**")
        exclude("**/resources/**")
        exclude("**/commonTest/**")
        exclude("**/jvmTest/**")
        exclude("**/androidUnitTest/**")
        exclude("**/androidInstrumentedTest/**")
        exclude("**/wrap/**")
    }
}
// ktlint has a bug where 'exclude' does not work, so this is a workaround
tasks {
    listOf(
        runKtlintCheckOverCommonMainSourceSet,
        runKtlintCheckOverCommonTestSourceSet
    ).forEach {
        it {
            setSource(
                project.sourceSets.map { sourceSet ->
                    sourceSet.allSource.filter { file ->
                        !file.path.contains("/generated/") && !file.path.contains("build.gradle.kts")
                    }
                }
            )
        }
    }
}
