plugins {
    id("java")
    id("application")
    eclipse
}

group = "org.gloom"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":llw"))
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

application {
    mainClass = "org.gloom.Launcher"
}

val nativeExtractDir = layout.buildDirectory.dir("lwjgl-natives")

tasks.withType<JavaExec>().configureEach {
    systemProperty("org.lwjgl.system.SharedLibraryExtractPath", nativeExtractDir.get().asFile.absolutePath)
}

tasks.register("llw-player-jar") {
    group = "build"
    description = "Builds the standalone llw-player fat JAR (alias for :llw-player:fatJar)"
    dependsOn(":llw-player:fatJar")
}

tasks.register<Exec>("buildDocs") {
    group = "documentation"
    description = "Build the VitePress documentation site"
    workingDir = file("docs")
    commandLine(
        if (System.getProperty("os.name").lowercase().contains("win")) "npm.cmd" else "npm",
        "run", "build"
    )
}
