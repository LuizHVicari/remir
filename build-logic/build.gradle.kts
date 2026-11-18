plugins {
    `kotlin-dsl`
}

dependencies {
    // `implementation` (not compileOnly) puts AGP/KGP/Exposed on the modules' build classpath,
    // so they apply the plugins by id only, with no `apply false` in the root build.
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.kotlin.serializationGradlePlugin)
    implementation(libs.kotlin.composeGradlePlugin)
    implementation(libs.exposed.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("kotlinJvmLibrary") {
            id = "remir.kotlin-jvm-library"
            implementationClass = "dev.luizhenriquevicari.remir.buildlogic.KotlinJvmLibraryConventionPlugin"
        }
        register("ktorServer") {
            id = "remir.ktor-server"
            implementationClass = "dev.luizhenriquevicari.remir.buildlogic.KtorServerConventionPlugin"
        }
        register("androidApp") {
            id = "remir.android-app"
            implementationClass = "dev.luizhenriquevicari.remir.buildlogic.AndroidAppConventionPlugin"
        }
    }
}
