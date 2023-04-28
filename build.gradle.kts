plugins {
    kotlin("multiplatform") version "1.8.20"
    kotlin("plugin.serialization") version "1.8.20"
    id("com.goncalossilva.resources") version "0.2.5"
    id("org.jlleitschuh.gradle.ktlint") version "11.3.1"
}

group = "io.polywrap"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    jvm {
        jvmToolchain(17)
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
//    js(IR) {
//        nodejs {
//            binaries.executable()
//        }
//    }
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

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.ensarsarajcic.kotlinx:serialization-msgpack:0.5.4")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                implementation("com.squareup.okio:okio:3.3.0") // fs plugin
                implementation("io.ktor:ktor-client-core:2.2.4") // http plugin
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
                implementation("com.goncalossilva:resources:0.3.1") // access resources in tests
                implementation("io.ktor:ktor-client-mock:2.2.4") // http plugin test
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("io.github.kawamuray.wasmtime:wasmtime-java:0.14.0")
                implementation("io.ktor:ktor-client-android:2.2.4") // http plugin
            }
        }
        val jvmTest by getting
//        val jsMain by getting {
//            dependencies {
//                implementation(npm("@polywrap/asyncify-js", "~0.10.0-pre"))
//                implementation("io.ktor:ktor-client-js:2.2.4") // http plugin
//                implementation("com.squareup.okio:okio-nodefilesystem:3.3.0") // fs plugin
//            }
//        }
//        val jsTest by getting
        val nativeMain by getting {
            dependencies {
                implementation("io.github.krisbitney:wasmtime-kt:1.0.0")
                when {
                    hostOs == "Mac OS X" -> implementation("io.ktor:ktor-client-curl:2.2.4")
                    hostOs == "Linux" -> implementation("io.ktor:ktor-client-curl:2.2.4")
                    isMingwX64 -> implementation("io.ktor:ktor-client-winhttp:2.2.4")
                }
            }
        }
        val nativeTest by getting
    }
}

// print stdout during tests
tasks.withType<Test> {
    this.testLogging {
        this.showStandardStreams = true
    }
}

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
