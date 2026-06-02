import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import java.io.File

fun org.gradle.api.Project.projectLibreExternalLibs() =
    fileTree(rootProject.file("projectlibre_contrib/lib")) {
        include("**/*.jar")
        exclude("flamingo-6.2.jar", "trident-6.2.jar")
    }

plugins {
    base
}

group = "com.projectlibre"
version = "0.0.3"

subprojects {
    apply(plugin = "java-library")

    repositories {
        mavenCentral()
    }

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(26))
        }
        withSourcesJar()
    }

    extensions.configure<SourceSetContainer>("sourceSets") {
        named("main") {
            java.setSrcDirs(listOf("src"))
            java.exclude("test/**")

            resources.setSrcDirs(listOf("src"))
            resources.exclude("**/*.java", "test/**")
        }
        named("test") {
            java.setSrcDirs(emptyList<String>())
            resources.setSrcDirs(emptyList<String>())
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(26)
    }

    tasks.withType<ProcessResources>().configureEach {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    tasks.withType<Jar>().configureEach {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    tasks.withType<Test>().configureEach {
        enabled = false
    }

    if (name != "projectlibre_contrib") {
        dependencies.add("implementation", projectLibreExternalLibs())
    }
}

tasks.register("stageAppDist") {
    group = "distribution"
    description = "Builds the installable application layout for ProjectLibre."
    dependsOn(":projectlibre_ui:installDist")
}

val releaseVersion = project.version.toString()
val releaseLabel = "v$releaseVersion"
val windowsReleaseRoot = layout.buildDirectory.dir("releases/$releaseLabel")
val windowsJpackageInput = windowsReleaseRoot.map { it.dir("jpackage-input") }
val windowsAppImageDir = windowsReleaseRoot.map { it.dir("app-image") }
val windowsMsiDir = windowsReleaseRoot.map { it.dir("msi") }
val docsDownloadsDir = layout.projectDirectory.dir("docs/downloads")
val jpackageJavaHomeProvider = providers.environmentVariable("JAVA_HOME")
    .orElse("C:\\Program Files\\Java\\jdk-26.0.1")

tasks.register<Sync>("prepareWindowsReleaseInput") {
    group = "distribution"
    description = "Prepares jpackage input files from the Gradle installDist output."
    dependsOn(":projectlibre_ui:installDist")

    val installLibDir = layout.projectDirectory.dir("projectlibre_ui/build/install/projectlibre_ui/lib")
    val iconFile = layout.projectDirectory.file("projectlibre_build/resources/wix/msi_images/projectlibre.ico")
    val licenseFile = layout.projectDirectory.file("projectlibre_build/license/license.txt")

    from(installLibDir)
    from(iconFile) {
        rename { "projectlibre.ico" }
    }
    from(licenseFile) {
        rename { "license.txt" }
    }
    into(windowsJpackageInput)

    doLast {
        val inputDir = windowsJpackageInput.get().asFile
        val iconPath = File(inputDir, "projectlibre.ico").absolutePath.replace('\\', '/')
        val associations = mapOf(
            "mpp.properties" to listOf(
                "extension=mpp",
                "mime-type=application/projectlibre",
                "description=MPP Project",
                "icon=$iconPath"
            ),
            "pod.properties" to listOf(
                "extension=pod",
                "mime-type=application/projectlibre",
                "description=ProjectLibre POD Project",
                "icon=$iconPath"
            ),
            "xml.properties" to listOf(
                "extension=xml",
                "mime-type=application/projectlibre",
                "description=MSPDI Project",
                "icon=$iconPath"
            )
        )

        associations.forEach { (name, lines) ->
            File(inputDir, name).writeText(lines.joinToString(System.lineSeparator()), Charsets.UTF_8)
        }
    }
}

tasks.register<Exec>("packageWindowsAppImage") {
    group = "distribution"
    description = "Builds the Windows app-image for the current Gradle version."
    dependsOn("prepareWindowsReleaseInput")
    onlyIf { System.getProperty("os.name").startsWith("Windows", ignoreCase = true) }

    doFirst {
        delete(windowsAppImageDir)
        windowsAppImageDir.get().asFile.mkdirs()
        val inputDir = windowsJpackageInput.get().asFile
        val javaHome = jpackageJavaHomeProvider.get()
        commandLine(
            File(javaHome, "bin/jpackage.exe").absolutePath,
            "--type", "app-image",
            "--name", "ProjectLibre",
            "--app-version", releaseVersion,
            "--input", inputDir.absolutePath,
            "--main-jar", "projectlibre_ui.jar",
            "--main-class", "com.projectlibre1.main.Main",
            "--icon", File(inputDir, "projectlibre.ico").absolutePath,
            "--license-file", File(inputDir, "license.txt").absolutePath,
            "--add-modules", "java.compiler,java.datatransfer,java.desktop,java.logging,java.naming,java.prefs,java.sql,java.xml,jdk.charsets",
            "--dest", windowsAppImageDir.get().asFile.absolutePath,
            "--verbose"
        )
    }
}

tasks.register<Exec>("packageWindowsMsi") {
    group = "distribution"
    description = "Builds the Windows MSI for the current Gradle version."
    dependsOn("prepareWindowsReleaseInput")
    onlyIf { System.getProperty("os.name").startsWith("Windows", ignoreCase = true) }

    doFirst {
        delete(windowsMsiDir)
        windowsMsiDir.get().asFile.mkdirs()
        val inputDir = windowsJpackageInput.get().asFile
        val javaHome = jpackageJavaHomeProvider.get()
        val wixBin = File(System.getProperty("user.home"), "AppData/Local/Programs/WiX Toolset v7.0/bin")
        environment("PATH", wixBin.absolutePath + File.pathSeparator + System.getenv("PATH"))
        commandLine(
            File(javaHome, "bin/jpackage.exe").absolutePath,
            "--type", "msi",
            "--name", "ProjectLibre",
            "--app-version", releaseVersion,
            "--input", inputDir.absolutePath,
            "--main-jar", "projectlibre_ui.jar",
            "--main-class", "com.projectlibre1.main.Main",
            "--icon", File(inputDir, "projectlibre.ico").absolutePath,
            "--license-file", File(inputDir, "license.txt").absolutePath,
            "--add-modules", "java.compiler,java.datatransfer,java.desktop,java.logging,java.naming,java.prefs,java.sql,java.xml,jdk.charsets",
            "--jlink-options", "--strip-native-commands --strip-debug --no-man-pages --no-header-files --compress zip-9",
            "--dest", windowsMsiDir.get().asFile.absolutePath,
            "--file-associations", File(inputDir, "mpp.properties").absolutePath,
            "--file-associations", File(inputDir, "pod.properties").absolutePath,
            "--file-associations", File(inputDir, "xml.properties").absolutePath,
            "--win-menu",
            "--win-shortcut",
            "--win-dir-chooser",
            "--verbose"
        )
    }
}

tasks.register<Zip>("packageWindowsZip") {
    group = "distribution"
    description = "Archives the Windows app-image as a downloadable ZIP."
    dependsOn("packageWindowsAppImage")

    from(windowsAppImageDir.map { it.dir("ProjectLibre") })
    archiveFileName.set("ProjectLibre-$releaseVersion-app-image.zip")
    destinationDirectory.set(docsDownloadsDir)

    doFirst {
        delete(docsDownloadsDir.file("ProjectLibre-$releaseVersion-app-image.zip"))
    }
}

tasks.register<Copy>("publishWindowsMsiToDocs") {
    group = "distribution"
    description = "Copies the Windows MSI into docs/downloads for GitHub Pages."
    dependsOn("packageWindowsMsi")

    from(windowsMsiDir.map { it.file("ProjectLibre-$releaseVersion.msi") })
    into(docsDownloadsDir)
}

tasks.register("publishReleaseToDocs") {
    group = "distribution"
    description = "Builds the Windows release artifacts and publishes them into docs/downloads."
    dependsOn("packageWindowsZip", "publishWindowsMsiToDocs")
}
