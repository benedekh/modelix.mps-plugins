import org.jetbrains.intellij.tasks.PrepareSandboxTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import java.util.zip.ZipInputStream

buildscript {
    dependencies {
        classpath("org.modelix.mps:build-tools-lib:1.3.0")
    }
}

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij") version "1.17.2"
}

group = "org.modelix.mps"
val mpsVersion = project.findProperty("mps.version").toString()
val mpsPlatformVersion = project.findProperty("mps.platform.version").toString().toInt()
val mpsHome = rootProject.layout.buildDirectory.dir("mps-$mpsVersion")

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
    withSourcesJar()
}

tasks.compileJava {
    options.release = 11
}

tasks.compileKotlin {
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += listOf("-Xjvm-default=all-compatibility")
        apiVersion = "1.6"
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
    sourceSets {
        main {
            languageSettings {
                apiVersion = KotlinVersion.KOTLIN_1_4.version
            }
        }
    }
}

dependencies {
    fun ModuleDependency.excludedBundledLibraries() {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core")
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-jdk8")
    }
    fun implementationWithoutBundled(dependencyNotation: String) {
        implementation(dependencyNotation) {
            excludedBundledLibraries()
        }
    }

//    implementation(coreLibs.ktor.server.html.builder)
    implementationWithoutBundled("io.ktor:ktor-server-html-builder:2.3.7")
    implementationWithoutBundled("io.ktor:ktor-server-netty:2.3.7")
    implementationWithoutBundled("io.ktor:ktor-server-cors:2.3.7")
    implementationWithoutBundled("io.ktor:ktor-server-status-pages:2.3.7")
    implementationWithoutBundled("io.github.microutils:kotlin-logging:3.0.5")
    implementationWithoutBundled("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.3")

    // contains RootDifferencePaneBase
//    compileOnly(mpsHome.map { it.files("plugins/mps-vcs/lib/vcs-platform.jar") })

    testImplementation(coreLibs.kotlin.coroutines.test)
    testImplementation(coreLibs.ktor.server.test.host)
    testImplementation(coreLibs.ktor.client.cio)
    testImplementation(libs.zt.zip)
//    testImplementation(libs.modelix.model.server)
//    testImplementation(libs.modelix.authorization)
//    testImplementation(coreLibs.kotlin.reflect)
//    implementation(libs.ktor.server.core)
//    implementation(libs.ktor.server.cors)
//    implementation(libs.ktor.server.netty)
//    implementation(libs.ktor.server.html.builder)
//    implementation(libs.ktor.server.auth)
//    implementation(libs.ktor.server.auth.jwt)
//    implementation(libs.ktor.server.status.pages)
//    implementation(libs.ktor.server.forwarded.header)
//    testImplementation(coreLibs.ktor.server.websockets)
//    testImplementation(coreLibs.ktor.server.content.negotiation)
//    testImplementation(coreLibs.ktor.server.resources)
//    implementation(libs.ktor.serialization.json)
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    localPath = mpsHome.map { it.asFile.absolutePath }
    instrumentCode = false
    plugins = listOf(
        "Git4Idea",
        "jetbrains.mps.vcs",
    )
}

tasks {
    patchPluginXml {
        sinceBuild.set("211") // 203 not supported, because VersionFixer was replaced by ModuleDependencyVersions in 211
        untilBuild.set("232.10072.781")
    }

    test {
        // tests currently fail for these versions
        enabled = !setOf(
            211, // jetbrains.mps.vcs plugin cannot be loaded
            212, // timeout because of some deadlock
            213, // timeout because of some deadlock
            222, // timeout because of some deadlock
        ).contains(mpsPlatformVersion)
    }

    buildSearchableOptions {
        enabled = false
    }

    runIde {
        autoReloadPlugins.set(true)
    }

    val shortPlatformVersion = mpsVersion.replace(Regex("""20(\d\d)\.(\d+).*"""), "$1$2")
    val mpsPluginDir = project.findProperty("mps$shortPlatformVersion.plugins.dir")?.toString()?.let { file(it) }
    if (mpsPluginDir != null && mpsPluginDir.isDirectory) {
        create<Sync>("installMpsPlugin") {
            dependsOn(prepareSandbox)
            from(buildDir.resolve("idea-sandbox/plugins/mps-diff-plugin"))
            into(mpsPluginDir.resolve("mps-diff-plugin"))
        }
    }

    withType<PrepareSandboxTask> {
        intoChild(pluginName.map { "$it/languages" })
            .from(project.layout.projectDirectory.dir("repositoryconcepts"))
    }

    val checkBinaryCompatibility by registering {
        group = "verification"
        doLast {
            val ignoredFiles = setOf(
                "META-INF/MANIFEST.MF",
            )
            fun loadEntries(fileName: String) = rootProject.layout.buildDirectory
                .dir("binary-compatibility")
                .dir(project.name)
                .file(fileName)
                .get().asFile.inputStream().use {
                    val zip = ZipInputStream(it)
                    val entries = generateSequence { zip.nextEntry }
                    entries.associate { it.name to "size:${it.size},crc:${it.crc}" }
                } - ignoredFiles
            val entriesA = loadEntries("a.jar")
            val entriesB = loadEntries("b.jar")
            val mismatches = (entriesA.keys + entriesB.keys).map { it to (entriesA[it] to entriesB[it]) }.filter { it.second.first != it.second.second }
            check(mismatches.isEmpty()) {
                "The following files have a different content:\n" + mismatches.joinToString("\n") { "  ${it.first}: ${it.second.first} != ${it.second.second}" }
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "org.modelix.mps"
            artifactId = "diff-plugin"
            artifact(tasks.buildPlugin) {
                extension = "zip"
            }
        }
    }
}

fun Provider<Directory>.dir(name: String): Provider<Directory> = map { it.dir(name) }
fun Provider<Directory>.file(name: String): Provider<RegularFile> = map { it.file(name) }
fun Provider<Directory>.dir(name: Provider<out CharSequence>): Provider<Directory> = flatMap { it.dir(name) }
