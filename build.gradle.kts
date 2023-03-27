plugins {
    kotlin("multiplatform") version "1.8.0"
    kotlin("plugin.serialization") version "1.8.0"
    id("com.goncalossilva.resources") version "0.2.5"
}

group = "eth.krisbitney"
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
    js(IR) {
        nodejs {
            binaries.executable()
        }
    }
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
//        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Mac OS X" -> macosArm64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.ensarsarajcic.kotlinx:serialization-msgpack:0.5.4")
                implementation("com.ensarsarajcic.kotlinx:serialization-msgpack-unsigned-support:0.5.4")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                implementation("com.squareup.okio:okio:3.3.0") // fs plugin
                implementation("io.ktor:ktor-client-core:2.2.4") // http plugin
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
                implementation("com.goncalossilva:resources:0.2.5") // access resources in tests
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("io.github.kawamuray.wasmtime:wasmtime-java:0.14.0")
                implementation("io.ktor:ktor-client-cio:2.2.4") // http plugin
            }
        }
        val jvmTest by getting
        val jsMain by getting {
            dependencies {
                implementation(npm("@polywrap/asyncify-js", "~0.10.0-pre"))
                implementation("io.ktor:ktor-client-js:2.2.4") // http plugin
                implementation("com.squareup.okio:okio-nodefilesystem:3.3.0") // fs plugin
            }
        }
        val jsTest by getting
        val nativeMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-cio:2.2.4") // http plugin
            }
        }
        val nativeTest by getting
    }
}

tasks.withType<Test> {
    this.testLogging {
        this.showStandardStreams = true
    }
}