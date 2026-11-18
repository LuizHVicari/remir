plugins {
    id("remir.kotlin-jvm-library")
}

// Contract between server and app: types, routes and format validation only.
// Do NOT add dependencies on Ktor server, Exposed, Android or Compose.
dependencies {
    api(platform(libs.ktor.bom))
    api(libs.ktor.resources)
    api(libs.kotlinx.serialization.json)
    api(libs.kotlinx.datetime)
    api(libs.konform)
}
