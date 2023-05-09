plugins {
    kotlin("multiplatform") version "1.8.20"
    kotlin("plugin.serialization") version "1.8.20"
    id("com.goncalossilva.resources") version "0.2.5"
    id("org.jlleitschuh.gradle.ktlint") version "11.3.1"
    id("com.android.library") version "7.4.2"
    id("uniffi-pipeline")
}

group = "io.polywrap"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    google()
    mavenCentral()
}

// Can I set this in a more elegant way?
val uniffiBindingsDir = "${buildDir}/generated/source/uniffi/kotlin"

kotlin {
    jvm {
        jvmToolchain(17)
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    android()

    val hostOs = System.getProperty("os.name")
    val arch = System.getProperty("os.arch")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> if (arch == "aarch64") {
            macosArm64("native")
        } else {
            macosX64("native")
        }
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    //    js(IR) {
//        nodejs {
//            binaries.executable()
//        }
//    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.ensarsarajcic.kotlinx:serialization-msgpack:0.5.5")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                implementation("com.squareup.okio:okio:3.3.0") // fs plugin
                implementation("io.ktor:ktor-client-core:2.3.0") // http plugin
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
            kotlin.srcDir(uniffiBindingsDir)
            dependencies {
                implementation("net.java.dev.jna:jna:5.13.0@aar") // JNA
                implementation("io.github.kawamuray.wasmtime:wasmtime-java:0.14.0")
                implementation("io.ktor:ktor-client-android:2.3.0") // http plugin
            }
        }
        val jvmTest by getting
        val androidMain by getting {
            dependsOn(sourceSets["jvmMain"])
            kotlin.srcDir(uniffiBindingsDir)
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
            }
        }
        val androidUnitTest by getting
        val androidInstrumentedTest by getting
        val nativeMain by getting {
            dependencies {
                implementation("io.github.krisbitney:wasmtime-kt:1.0.0")
                when {
                    hostOs == "Mac OS X" -> implementation("io.ktor:ktor-client-curl:2.3.0")
                    hostOs == "Linux" -> implementation("io.ktor:ktor-client-curl:2.3.0")
                    isMingwX64 -> implementation("io.ktor:ktor-client-winhttp:2.3.0")
                }
            }
        }
        val nativeTest by getting

        //        val jsMain by getting {
//            dependencies {
//                implementation(npm("@polywrap/asyncify-js", "~0.10.0-pre"))
//                implementation("io.ktor:ktor-client-js:2.3.0") // http plugin
//                implementation("com.squareup.okio:okio-nodefilesystem:3.3.0") // fs plugin
//            }
//        }
//        val jsTest by getting
    }
}

uniffi {
    rustClientRepoBranch = "main"
    clonesDir = "$projectDir/clones"
    desktopJniPath = "${project.buildDir}/jniLibs"
    androidJniPath = "${projectDir}/src/androidMain/jniLibs"
    rustTargets = listOf(
//        "armv7-linux-androideabi",
//        "i686-linux-android",
//        "aarch64-linux-android",
//        "x86_64-linux-android",
        "x86_64-apple-darwin",
        "aarch64-apple-darwin",
//        "x86_64-pc-windows-gnu",
        // "aarch64-unknown-linux-gnu"
        // "x86_64-unknown-linux-gnu"
    )
    libname = "polywrap_native"
    bindingsDir = uniffiBindingsDir
    isRelease = false
}

android {
    namespace = "io.polywrap"
    compileSdk = 32
    defaultConfig {
        minSdk = 24
        targetSdk = 32
    }
    ndkVersion = "22.1.7171670"
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    // make sure dynamic library is copied before assembling android packages
    libraryVariants.all {
        tasks.named("assemble${name.capitalize()}") {
            dependsOn(tasks.getByName("copyNativeLibraryForAndroid"))
        }
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
    dependsOn(tasks.getByName("copyNativeLibraryForDesktop"))
    val uniffi = project.extensions.getByName("uniffi") as UniffiPipelineConfig
    from(uniffi.desktopJniPath)
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
        exclude("**/jsTest/**")
        exclude("**/nativeTest/**")
        exclude("**/wrap/**")
        exclude("**/wrapHardCoded/**")
    }
}
