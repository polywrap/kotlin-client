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
version = "1.0-SNAPSHOT"

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
                implementation("io.ktor:ktor-client-core:2.3.0") // http plugin
                implementation("io.ktor:ktor-client-android:2.3.0") // http plugin
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
                implementation("com.goncalossilva:resources:0.3.2") // access resources in tests
                implementation("io.ktor:ktor-client-mock:2.3.0") // http plugin test
                implementation("com.ionspin.kotlin:bignum:0.3.8") // client test
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0") // client test
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
    desktopJniPath = "${project.buildDir}/jniLibs"
    androidJniPath = "$projectDir/src/androidMain/jniLibs"
    rustTargets = listOf(
        "armv7-linux-androideabi",
        "i686-linux-android",
        "aarch64-linux-android",
        "x86_64-linux-android",
        "x86_64-pc-windows-gnu",
        "aarch64-unknown-linux-gnu",
        "x86_64-unknown-linux-gnu",
        "x86_64-apple-darwin",
        "aarch64-apple-darwin"
    )
    libname = "polywrap_native"
    bindingsDir = uniffiBindingsDir
    // when false, only builds for debug on current desktop platform even if its not in the list
    isRelease = false
}

android {
    namespace = "io.polywrap"
    compileSdk = 32
    compileOptions {
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// make sure dynamic library is available during jvm tests
tasks.named<Test>("jvmTest") {
    doFirst {
        val uniffi = project.extensions.getByName("uniffi") as UniffiPipelineConfig
        systemProperty("java.library.path", uniffi.desktopJniPath)
    }
}

// make sure dynamic library is packaged with jvm release
tasks.named<Jar>("jvmJar") {
    val uniffi = project.extensions.getByName("uniffi") as UniffiPipelineConfig
    from(uniffi.desktopJniPath)
}

// javadoc generation for Maven repository publication
tasks.register<Jar>("dokkaJavadocJar") {
    dependsOn(tasks.dokkaJavadoc)
    from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
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
        exclude("**/generated/**")
        exclude("**/commonTest/**")
        exclude("**/jvmTest/**")
        exclude("**/androidUnitTest/**")
        exclude("**/androidInstrumentedTest/**")
        exclude("**/wrap/**")
        exclude("**/wrapHardCoded/**")
    }
}
