package dev.luizhenriquevicari.remir.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.version(alias: String): String = findVersion(alias).get().requiredVersion

internal fun VersionCatalog.library(alias: String): Provider<MinimalExternalModuleDependency> =
    findLibrary(alias).get()

internal val Project.jvmVersion: Int
    get() = libs.version("jvm").toInt()

/** JUnit Platform + explicit launcher (required by Gradle 9). */
internal fun Project.configureJUnit() {
    dependencies.add("testImplementation", dependencies.platform(libs.library("junit-bom")))
    dependencies.add("testImplementation", libs.library("junit-jupiter"))
    dependencies.add("testImplementation", "org.jetbrains.kotlin:kotlin-test-junit5")
    dependencies.add("testRuntimeOnly", libs.library("junit-platform-launcher"))
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}
