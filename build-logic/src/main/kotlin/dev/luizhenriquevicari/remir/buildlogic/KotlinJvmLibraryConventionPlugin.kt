package dev.luizhenriquevicari.remir.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

/** Plain Kotlin/JVM module with kotlinx.serialization (used by `core`). */
class KotlinJvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.jvm")
        pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")

        extensions.configure<KotlinJvmProjectExtension> {
            jvmToolchain(jvmVersion)
        }
        configureJUnit()
    }
}
