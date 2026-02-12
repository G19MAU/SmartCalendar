import java.io.File
// settings.gradle.kts


rootProject.name = "SmartCalendar"

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
// --- Robust Android SDK detection & local.properties bootstrap ---

fun hasValidSdk(path: String, requiredApi: Int = 35): Boolean {
    val base = File(path)
    if (!base.isDirectory) return false
    val platformOk = File(base, "platforms/android-$requiredApi").exists()
    val toolsOk = File(base, "platform-tools").exists()
    val cliOk = File(base, "cmdline-tools").exists()
    return platformOk || (toolsOk && cliOk)
}

fun discoverSdk(): String? {
    val home = System.getProperty("user.home")
    val candidates = listOfNotNull(
        System.getenv("ANDROID_SDK_ROOT"),
        System.getenv("ANDROID_HOME"),
        "$home/Library/Android/sdk",   // macOS default
        "$home/Android/Sdk",           // Linux default
        "C:/Android/sdk",
        "C:/Users/${System.getProperty("user.name")}/AppData/Local/Android/Sdk"
    )
    return candidates.firstOrNull { it != null && hasValidSdk(it, 35) }
}

// Controls
val forceAndroid = providers.gradleProperty("forceAndroid").isPresent ||
        (System.getenv("FORCE_ANDROID") == "true")
val sdkPath = discoverSdk()
val isIDE = (System.getProperty("idea.active") == "true")

// If SDK is valid, ensure local.properties exists (root + app for safety)
if (sdkPath != null) {
    val rootLp = File(rootDir, "local.properties")
    if (!rootLp.exists()) {
        rootLp.writeText("sdk.dir=${sdkPath.replace("\\", "\\\\")}\n")
        println("Generated local.properties with sdk.dir=$sdkPath")
    }
    val appLp = File(rootDir, "app/local.properties")
    if (!appLp.exists()) {
        appLp.parentFile.mkdirs()
        appLp.writeText("sdk.dir=${sdkPath.replace("\\", "\\\\")}\n")
        println("Generated app/local.properties with sdk.dir=$sdkPath")
    }
}

// Decide whether to include :app (Android) module
val includeAndroid = forceAndroid || isIDE || (sdkPath != null)

if (includeAndroid) {
    include(":app")
    println("Including :app (Android). Reason -> force:$forceAndroid, IDE:$isIDE, sdk:${sdkPath != null}")
} else {
    println("Skipping :app — no valid Android SDK detected (and no override).")
}
