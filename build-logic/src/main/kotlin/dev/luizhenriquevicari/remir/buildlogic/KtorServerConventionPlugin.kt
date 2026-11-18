package dev.luizhenriquevicari.remir.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

/**
 * Ktor server: Kotlin/JVM + serialization + `application` (provides `./gradlew :server:run`)
 * + the Exposed Gradle plugin (provides `./gradlew :server:generateMigrations`).
 */
class KtorServerConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.jvm")
        pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")
        pluginManager.apply("application")
        // Applied here, not in the module's `plugins {}`: only plugins declared by build-logic resolve by id there.
        pluginManager.apply("org.jetbrains.exposed.plugin")

        extensions.configure<KotlinJvmProjectExtension> {
            jvmToolchain(jvmVersion)
        }
        dependencies.add("implementation", dependencies.platform(libs.library("ktor-bom")))
        dependencies.add("testImplementation", dependencies.platform(libs.library("ktor-bom")))
        configureJUnit()
    }
}
