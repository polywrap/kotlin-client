import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.register
import java.io.File

val config = project.extensions.create<UniffiPipelineConfig>("uniffi")

afterEvaluate {

    // clone rust client repository
    val cloneRustClient = tasks.register<Exec>("cloneRustClient") {
        group = "uniffi"
        val cwd = File(config.clonesDir)
        doFirst { cwd.mkdirs() }
        workingDir(cwd)
        val uri = "https://github.com/polywrap/rust-client.git"
        val branch = "main"
        commandLine("git", "clone", "-b", branch, "--depth", "1", "--single-branch", uri)
        onlyIf { !File(config.rustClientRepoCloneDir).exists() }
    }

    // add rust targets
    config.rustTargets.map {
        tasks.register<Exec>("rustupAddTarget_$it") {
            group = "uniffi"
            commandLine("rustup", "target", "add", it, "--toolchain", "nightly")
            dependsOn(cloneRustClient)
        }
    }

    // cargo build
    config.rustTargets.map { target ->
        tasks.register<Exec>("cargoBuild_$target") {
            group = "uniffi"
            workingDir("${config.rustClientRepoCloneDir}/packages/native")
            val profile = if (config.isRelease) "release" else "dev"
            commandLine("cargo", "+nightly", "build", "--target", target, "--profile", profile)
            dependsOn(tasks.getByName("rustupAddTarget_$target"))
        }
    }

    // copy dynamic library for android
    val copyNativeLibraryForAndroidTasks = config.rustTargets
        .filter { it.contains("android") }
        .map { target ->
            tasks.register<Copy>("copyNativeLibrary_$target") {
                val profile = if (config.isRelease) "release" else "debug"
                from("${config.rustClientRepoCloneDir}/target/$target/$profile")
                into("${config.androidJniPath}/${rustTargetToAndroidAbiOrDesktopValue(target)}")
                val libname = config.libname
                include("lib$libname.so", "lib$libname.dylib", "$libname.dll")
                dependsOn(tasks.getByName("cargoBuild_$target"))
            }
        }
    val copyNativeLibraryForAndroid: TaskProvider<Task> = tasks.register("copyNativeLibraryForAndroid") {
        group = "uniffi"
        dependsOn(copyNativeLibraryForAndroidTasks)
    }

    // copy dynamic library for desktop
    val copyNativeLibraryForDesktopTasks = config.rustTargets
        .filter { !it.contains("android") }
        .map { target ->
            tasks.register<Copy>("copyNativeLibrary_$target") {
                val profile = if (config.isRelease) "release" else "debug"
                from("${config.rustClientRepoCloneDir}/target/$target/$profile")
                into("${config.desktopJniPath}/${rustTargetToAndroidAbiOrDesktopValue(target)}")
                val libname = config.libname
                include("lib$libname.so", "lib$libname.dylib", "$libname.dll")
                dependsOn(tasks.getByName("cargoBuild_$target"))
            }
        }
    val copyNativeLibraryForDesktop: TaskProvider<Task> = tasks.register("copyNativeLibraryForDesktop") {
        group = "uniffi"
        dependsOn(copyNativeLibraryForDesktopTasks)
    }

// clone UniFFI Kotlin MPP Bindings repository
//tasks.register<Exec>("cloneUniffiKotlinMppBindings") {
//    val uri = "https://gitlab.com/trixnity/uniffi-kotlin-multiplatform-bindings.git"
//    val branch = "main"
//    commandLine("git", "clone", "-b", branch, "--depth", "1", "--single-branch", uri)
//    val cwd = File(clonesDir)
//    cwd.mkdirs()
//    workingDir(cwd)
//    onlyIf { !File(uniffiKotlinMppBindingsDir).exists() }
//}

    // run full pipeline
    tasks.register("uniffi") {
        group = "uniffi"
        dependsOn(copyNativeLibraryForAndroid, copyNativeLibraryForDesktop)
    }
}
