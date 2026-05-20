plugins {
    application
}

import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

group = "org.llw.player"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":llw-runtime"))
    implementation(project(":llw"))

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

application {
    mainClass = "org.llw.player.PlayerLauncher"
}

val nativeExtractDir = layout.buildDirectory.dir("lwjgl-natives")

tasks.withType<JavaExec>().configureEach {
    systemProperty("org.lwjgl.system.SharedLibraryExtractPath", nativeExtractDir.get().asFile.absolutePath)
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<Jar>("fatJar") {
    archiveFileName.set("llw-player-all.jar")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    dependsOn(configurations.runtimeClasspath)
    from(sourceSets.main.get().output)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    }) {
        exclude(
            "META-INF/*.SF",
            "META-INF/*.DSA",
            "META-INF/*.RSA",
            "META-INF/LICENSE*",
            "META-INF/NOTICE*",
            "META-INF/MANIFEST.MF"
        )
        exclude("META-INF/services/com.oracle.truffle.api.provider.TruffleLanguageProvider")
        exclude("META-INF/services/org.graalvm.polyglot.impl.AbstractPolyglotImpl")
    }
    manifest {
        attributes["Main-Class"] = "org.llw.player.PlayerLauncher"
    }
    doLast {
        val runtimeClasspath = configurations.runtimeClasspath.get().files
        val jarFile = archiveFile.get().asFile
        mergeServiceFile(
            jarFile,
            runtimeClasspath,
            "META-INF/services/com.oracle.truffle.api.provider.TruffleLanguageProvider"
        )
        mergeServiceFile(
            jarFile,
            runtimeClasspath,
            "META-INF/services/org.graalvm.polyglot.impl.AbstractPolyglotImpl",
            preferredProvider = "com.oracle.truffle.polyglot.PolyglotImpl"
        )
    }
}

fun mergeServiceFile(
    jarFile: File,
    classpath: Collection<File>,
    servicePath: String,
    preferredProvider: String? = null,
) {
    val providers = linkedSetOf<String>()
    fun collect(from: File) {
        if (!from.isFile || from.extension != "jar") {
            return
        }
        ZipFile(from).use { zip ->
            zip.getEntry(servicePath)?.let { entry ->
                zip.getInputStream(entry).bufferedReader().useLines { lines ->
                    lines.map { it.trim() }
                        .filter { it.isNotEmpty() && !it.startsWith("#") }
                        .forEach { providers.add(it) }
                }
            }
        }
    }
    classpath.forEach { collect(it) }
    collect(jarFile)
    if (providers.isEmpty()) {
        return
    }
    val mergedProviders = if (preferredProvider == null) {
        providers
    } else {
        linkedSetOf(
            preferredProvider.takeIf { providers.contains(it) }
                ?: providers.firstOrNull { !it.contains("Enterprise", ignoreCase = true) }
                ?: providers.first()
        )
    }
    val tempFile = File.createTempFile("llw-fatjar", ".jar")
    ZipOutputStream(tempFile.outputStream()).use { out ->
        val written = linkedSetOf<String>()
        ZipFile(jarFile).use { zip ->
            zip.entries().asIterator().forEachRemaining { entry ->
                if (entry.name == servicePath || !written.add(entry.name)) {
                    return@forEachRemaining
                }
                out.putNextEntry(ZipEntry(entry.name))
                zip.getInputStream(entry).transferTo(out)
                out.closeEntry()
            }
        }
        written.add(servicePath)
        out.putNextEntry(ZipEntry(servicePath))
        out.write((mergedProviders.joinToString("\n") + "\n").toByteArray(Charsets.UTF_8))
        out.closeEntry()
    }
    check(jarFile.delete()) { "Could not replace fat jar: $jarFile" }
    check(tempFile.renameTo(jarFile)) { "Could not write merged fat jar: $jarFile" }
}

tasks.jar {
    dependsOn(tasks.named("fatJar"))
}

tasks.register<Exec>("jpackageImage") {
    group = "distribution"
    description = "Creates a Windows app-image using jpackage"
    dependsOn("fatJar")
    val fatJarFile = layout.buildDirectory.file("libs/llw-player-all.jar").get().asFile
    val outputDir = layout.buildDirectory.dir("jpackage").get().asFile
    val jpackageBin = "${System.getProperty("java.home")}/bin/jpackage${if (System.getProperty("os.name").lowercase().contains("win")) ".exe" else ""}"
    doFirst {
        outputDir.mkdirs()
    }
    commandLine(
        jpackageBin,
        "--type", "app-image",
        "--name", "LLWPlayer",
        "--input", fatJarFile.parentFile.absolutePath,
        "--main-jar", fatJarFile.name,
        "--main-class", "org.llw.player.PlayerLauncher",
        "--dest", outputDir.absolutePath
    )
}
