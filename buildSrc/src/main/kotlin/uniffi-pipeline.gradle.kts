import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.register
import java.io.File

val config = project.extensions.create<UniffiPipelineConfig>("uniffi")

fun getCurrentDesktopPlatform(): String {
    val hostOs: String = System.getProperty("os.name")
    val arch: String = System.getProperty("os.arch").replace("amd64", "x86_64")
    return arch + when {
        hostOs == "Mac OS X" -> "-apple-darwin"
        hostOs == "Linux" -> "-unknown-linux-gnu"
        hostOs.startsWith("Windows") -> "-pc-windows-gnu"
        else -> throw Exception("Unknown host OS. \n${hostOs}")
    }
}

afterEvaluate {
    val rustClientRepoCloneDir = "${config.clonesDir}/rust-client"
    val packageDir = "${rustClientRepoCloneDir}/packages/native"
    val udl = "$packageDir/src/main.udl"

    // Adjust parameters if not release build
    val rustTargets = if (config.isRelease) {
        config.rustTargets
    } else {
        listOf(getCurrentDesktopPlatform())
    }

    // clone rust client repository
    val cloneRustClient = tasks.register<Exec>("cloneRustClient") {
        group = "uniffi"
        val cwd = File(config.clonesDir)
        doFirst { cwd.mkdirs() }
        workingDir(cwd)
        val uri = "https://github.com/polywrap/rust-client.git"
        val branch = config.rustClientRepoBranch
        commandLine("git", "clone", "-b", branch, "--depth", "1", "--single-branch", uri)
        doLast {
            val dotGitPath = "${config.clonesDir}/rust-client/.git/"
            File(dotGitPath).deleteRecursively()
        }
        onlyIf { !File(rustClientRepoCloneDir).exists() }
    }

    val addOpensslDependency = tasks.register("addOpensslDependency") {
        group = "uniffi"
        doFirst {
            val cargoToml = File("$packageDir/Cargo.toml")
            val newContents = cargoToml.readText().replace(
                "[dependencies]",
                "[dependencies]\nopenssl = { version = \"0.10\", features = [\"vendored\"] }"
            )
            cargoToml.writeText(newContents)
        }
        dependsOn(cloneRustClient)
    }

    // add rust targets
    rustTargets.map {
        tasks.register<Exec>("rustupAddTarget_$it") {
            group = "uniffi"
            commandLine("rustup", "target", "add", it)
            dependsOn(cloneRustClient)
        }
    }

    // cargo build
    rustTargets.map { target ->
        tasks.register<Exec>("cargoBuild_$target") {
            group = "uniffi"
            workingDir(packageDir)
            commandLine("cross", "build", "--target", target, "--profile", "release")
            dependsOn(tasks.getByName("rustupAddTarget_$target"))
            dependsOn(addOpensslDependency)
        }
    }

    // copy dynamic library for android
    val copyNativeLibraryForAndroidTasks = rustTargets
        .filter { it.contains("android") }
        .map { target ->
            tasks.register<Copy>("copyNativeLibrary_$target") {
                from("${rustClientRepoCloneDir}/target/$target/release")
                into("${config.androidJnaPath}/${rustTargetToAndroidAbiOrDesktopValue(target)}")
                val packageName = config.packageName
                include("lib$packageName.so", "lib$packageName.dylib", "$packageName.dll")
                rename(packageName, config.libname)
                dependsOn(tasks.getByName("cargoBuild_$target"))
            }
        }
    val copyNativeLibraryForAndroid: TaskProvider<Task> = tasks.register("copyNativeLibraryForAndroid") {
        group = "uniffi"
        dependsOn(copyNativeLibraryForAndroidTasks)
    }

    // copy dynamic library for desktop
    val copyNativeLibraryForDesktopTasks = rustTargets
        .filter { !it.contains("android") }
        .map { target ->
            tasks.register<Copy>("copyNativeLibrary_$target") {
                from("${rustClientRepoCloneDir}/target/$target/release")
                into("${config.desktopJnaPath}/${rustTargetToAndroidAbiOrDesktopValue(target)}")
                val packageName = config.packageName
                include("lib$packageName.so", "lib$packageName.dylib", "$packageName.dll")
                rename(packageName, config.libname)
                dependsOn(tasks.getByName("cargoBuild_$target"))
            }
        }
    val copyNativeLibraryForDesktop: TaskProvider<Task> = tasks.register("copyNativeLibraryForDesktop") {
        group = "uniffi"
        dependsOn(copyNativeLibraryForDesktopTasks)
    }

    val generateKotlinUniffiBindings = tasks.register<Exec>("generateKotlinUniffiBindings") {
        group = "uniffi"
        workingDir(rustClientRepoCloneDir)
        val bin = "${rustClientRepoCloneDir}/target/${rustTargets[0]}/release/uniffi-bindgen"
        val command = "$bin generate $udl --language kotlin --out-dir ${config.bindingsDir}"
        commandLine(command.split(" "))
        dependsOn(copyNativeLibraryForAndroid, copyNativeLibraryForDesktop)
        onlyIf { rustTargets.isNotEmpty() }
    }

    // run full pipeline
    tasks.register("uniffi") {
        group = "uniffi"
        dependsOn(generateKotlinUniffiBindings)
    }
}
