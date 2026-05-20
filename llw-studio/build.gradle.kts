plugins {
    application
    eclipse
}

group = "org.llw.studio"
version = "1.0-SNAPSHOT"

val imguiVersion = "1.86.12"

val imguiNatives = when {
    System.getProperty("os.name").startsWith("Windows") -> "imgui-java-natives-windows"
    System.getProperty("os.name").let { it.startsWith("Mac OS X") || it.startsWith("Darwin") } ->
        "imgui-java-natives-macos"
    else -> "imgui-java-natives-linux"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":llw-runtime"))
    implementation(project(":llw"))
    implementation("io.github.spair:imgui-java-binding:$imguiVersion")
    implementation("io.github.spair:imgui-java-lwjgl3:$imguiVersion")
    runtimeOnly("io.github.spair:$imguiNatives:$imguiVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.2")
    implementation("org.graalvm.polyglot:polyglot:24.1.1")
    implementation("org.graalvm.js:js:24.1.1")
    implementation("org.jbox2d:jbox2d-library:2.2.1.1")
    implementation("com.github.weisj:jsvg:2.0.0")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
    withJavadocJar()
    withSourcesJar()
}

application {
    mainClass = "org.llw.studio.StudioLauncher"
}

val nativeExtractDir = layout.buildDirectory.dir("lwjgl-natives")

tasks.withType<JavaExec>().configureEach {
    systemProperty("org.lwjgl.system.SharedLibraryExtractPath", nativeExtractDir.get().asFile.absolutePath)
}

tasks.test {
    useJUnitPlatform()
}

tasks.named<ProcessResources>("processResources") {
    dependsOn(":llw-player:fatJar")
    from(project(":llw-player").layout.buildDirectory.file("libs/llw-player-all.jar")) {
        into("player")
        rename { "llw-player.jar" }
    }
}

tasks.javadoc {
    dependsOn(":llw:javadoc")
    options {
        (this as StandardJavadocDocletOptions).apply {
            source = "17"
            val llwJavadocUrl = project(":llw").layout.buildDirectory.dir("docs/javadoc").get().asFile.toURI().toString()
            links(
                "https://www.javadoc.io/doc/org.lwjgl/lwjgl/3.4.1",
                llwJavadocUrl
            )
            addStringOption("Xdoclint:none")
        }
    }
}
