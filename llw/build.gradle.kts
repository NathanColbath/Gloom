plugins {
    `java-library`
    eclipse
}

group = "org.llw"
version = "1.0-SNAPSHOT"

val lwjglVersion = "3.4.1"

val lwjglNatives = Pair(
    System.getProperty("os.name")!!,
    System.getProperty("os.arch")!!
).let { (name, arch) ->
    when {
        arrayOf("Linux", "SunOS", "Unit").any { name.startsWith(it) } ->
            when {
                arch.startsWith("aarch64") -> "natives-linux-arm64"
                arch.startsWith("arm") || arch.startsWith("arm32") -> "natives-linux-arm32"
                arch.startsWith("ppc") -> "natives-linux-ppc64le"
                arch.startsWith("riscv") -> "natives-linux-riscv64"
                else -> "natives-linux"
            }

        arrayOf("Mac OS X", "Darwin").any { name.startsWith(it) } ->
            if (arch.startsWith("aarch64")) "natives-macos-arm64" else "natives-macos"

        arrayOf("Windows").any { name.startsWith(it) } ->
            when {
                arch.startsWith("aarch64") -> "natives-windows-arm64"
                arch.contains("64") -> "natives-windows"
                else -> "natives-windows-x86"
            }

        else -> throw GradleException("Unsupported platform: $name ($arch)")
    }
}

val lwjglModules = listOf(
    "lwjgl",
    "lwjgl-assimp",
    "lwjgl-bgfx",
    "lwjgl-egl",
    "lwjgl-fmod",
    "lwjgl-freetype",
    "lwjgl-glfw",
    "lwjgl-harfbuzz",
    "lwjgl-hwloc",
    "lwjgl-jawt",
    "lwjgl-jemalloc",
    "lwjgl-ktx",
    "lwjgl-llvm",
    "lwjgl-lmdb",
    "lwjgl-lz4",
    "lwjgl-meshoptimizer",
    "lwjgl-msdfgen",
    "lwjgl-nanovg",
    "lwjgl-nfd",
    "lwjgl-nuklear",
    "lwjgl-odbc",
    "lwjgl-openal",
    "lwjgl-opencl",
    "lwjgl-opengl",
    "lwjgl-opengles",
    "lwjgl-openxr",
    "lwjgl-opus",
    "lwjgl-par",
    "lwjgl-remotery",
    "lwjgl-renderdoc",
    "lwjgl-rpmalloc",
    "lwjgl-sdl",
    "lwjgl-shaderc",
    "lwjgl-spng",
    "lwjgl-spvc",
    "lwjgl-stb",
    "lwjgl-tinyexr",
    "lwjgl-tinyfd",
    "lwjgl-vma",
    "lwjgl-vulkan",
    "lwjgl-xxhash",
    "lwjgl-yoga",
    "lwjgl-zstd",
)

val lwjglModulesWithoutNatives = setOf(
    "lwjgl-egl",
    "lwjgl-fmod",
    "lwjgl-jawt",
    "lwjgl-odbc",
    "lwjgl-opencl",
    "lwjgl-renderdoc",
    "lwjgl-vulkan",
)

repositories {
    mavenCentral()
}

dependencies {
    api(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

    lwjglModules.forEach { module ->
        api("org.lwjgl:$module")
    }

    lwjglModules
        .filter { it !in lwjglModulesWithoutNatives }
        .forEach { module ->
            runtimeOnly("org.lwjgl:$module::$lwjglNatives")
        }

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

val nativeExtractDir = layout.buildDirectory.dir("lwjgl-natives")

tasks.withType<JavaExec>().configureEach {
    systemProperty("org.lwjgl.system.SharedLibraryExtractPath", nativeExtractDir.get().asFile.absolutePath)
}

tasks.test {
    useJUnitPlatform()
}

tasks.javadoc {
    options {
        (this as StandardJavadocDocletOptions).apply {
            source = "17"
            links("https://www.javadoc.io/doc/org.lwjgl/lwjgl/3.4.1")
            addStringOption("Xdoclint:none")
        }
    }
}
