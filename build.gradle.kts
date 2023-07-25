import java.util.*

plugins {
    kotlin("multiplatform") version "1.7.20"
    id("org.jetbrains.dokka") version "1.8.20"
    id("maven-publish")
}

val props = Properties()
file("local.properties").inputStream().let { props.load(it) }

repositories {
    mavenCentral()
}

group = "eu.syrou"
version = "0.0.5-SNAPSHOT"

val mingwPath = File(System.getenv("MINGW64_DIR") ?: "C:/msys64/mingw64")

kotlin {
    mingwX64("mingwX64").apply {
        binaries {
            executable {
                entryPoint = "main"
                linkerOpts(
                    "-L${mingwPath.resolve("lib")}"
                )
            }
        }
    }

    /*macosX64("macosX64"){
        binaries {
            executable {
                entryPoint = "main"
                linkerOpts("-L/opt/local/lib", "-L/usr/local/lib")
            }
        }
    }*/

    linuxX64("linuxX64"){
        binaries {
            executable {
                entryPoint = "main"
                linkerOpts("-L/usr/lib/x86_64-linux-gnu", "-L/usr/lib64")
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
            }
        }
        val linuxX64Main by getting
        val mingwX64Main by getting
        //val macosX64Main by getting
        all {
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
            languageSettings.optIn("kotlin.ExperimentalStdlibApi")
            languageSettings.optIn("kotlinx.cinterop.UnsafeNumber")
            languageSettings.optIn("kotlin.native.runtime.NativeRuntimeApi")
        }
    }
}

publishing {
    repositories {
        /*maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Syrou/console-lifecycle-scope")
            credentials {
                username = (props.getProperty("gpr.user") ?: System.getenv("USERNAME")).toString()
                password = (props.getProperty("gpr.key") ?: System.getenv("TOKEN")).toString()
            }
        }*/
    }
}
