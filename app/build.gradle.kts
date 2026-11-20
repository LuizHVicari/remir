import com.android.build.api.variant.BuildConfigField
import java.util.Properties

plugins {
    id("remir.android-app")
}

// Debug config is machine-local: it comes from local.properties (NEVER committed).
// Release config is not: it must come from a Gradle property or an environment variable, so a release
// never depends on one developer's machine. See README.
val localProperties =
    Properties().apply {
        val file = rootProject.file("local.properties")
        if (file.exists()) file.inputStream().use(::load)
    }

fun localProperty(
    key: String,
    default: String,
): String = localProperties.getProperty(key, default)

/**
 * Release value from `-P<property>` (or `~/.gradle/gradle.properties`), else the `<envVar>` environment
 * variable. Lazy: it only fails when the release BuildConfig is generated, never for debug builds.
 */
fun requiredForRelease(
    property: String,
    envVar: String,
): Provider<String> =
    providers
        .gradleProperty(property)
        .orElse(providers.environmentVariable(envVar))
        .orElse("")
        .map { value ->
            require(value.isNotBlank()) {
                "Release builds need the Gradle property '$property' or the environment variable '$envVar' (see README)."
            }
            value
        }

/** Optional value from `-P<property>`, else the `<envVar>` environment variable, else null. */
fun optionalSetting(
    property: String,
    envVar: String,
): String? =
    providers
        .gradleProperty(property)
        .orElse(providers.environmentVariable(envVar))
        .orNull
        ?.takeIf { it.isNotBlank() }

// Signing is optional: without a keystore `assembleRelease` still works, but the APK is unsigned (not installable).
val signingStoreFile = optionalSetting("remir.keystore.file", "REMIR_KEYSTORE_FILE")
val signingStorePassword = optionalSetting("remir.keystore.password", "REMIR_KEYSTORE_PASSWORD")
val signingKeyAlias = optionalSetting("remir.key.alias", "REMIR_KEY_ALIAS")
// A PKCS12 keystore uses the same password for the store and the key, so this one is optional.
val signingKeyPassword = optionalSetting("remir.key.password", "REMIR_KEY_PASSWORD")

android {
    namespace = "dev.luizhenriquevicari.remir.app"

    defaultConfig {
        // PERMANENT once published on the Play Store. Never change it.
        applicationId = "dev.luizhenriquevicari.remir"
        // CI passes the real values (see README); these defaults are for local builds.
        versionCode = optionalSetting("remir.versionCode", "REMIR_VERSION_CODE")?.toInt() ?: 1
        versionName = optionalSetting("remir.versionName", "REMIR_VERSION_NAME") ?: "0.1.0"
    }

    signingConfigs {
        val storeFilePath = signingStoreFile
        val storePass = signingStorePassword
        val alias = signingKeyAlias
        if (storeFilePath != null && storePass != null && alias != null) {
            create("release") {
                storeFile = file(storeFilePath)
                storePassword = storePass
                keyAlias = alias
                keyPassword = signingKeyPassword ?: storePass
            }
        }
    }

    buildTypes {
        debug {
            buildConfigField(
                "String",
                "CLERK_PUBLISHABLE_KEY",
                "\"${localProperty("clerk.publishableKey", "")}\"",
            )
            // 10.0.2.2 is the host machine's localhost as seen from the emulator.
            buildConfigField(
                "String",
                "API_URL",
                "\"${localProperty("api.url", "http://10.0.2.2:8080/")}\"",
            )
        }
        release {
            signingConfig = signingConfigs.findByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

androidComponents {
    onVariants(selector().withBuildType("release")) { variant ->
        val apiUrl =
            requiredForRelease("remir.api.url", "REMIR_API_URL").map { url ->
                require(
                    url.startsWith("https://"),
                ) { "remir.api.url must be https:// in a release build (got '$url')." }
                url
            }
        val clerkKey = requiredForRelease("remir.clerk.publishableKey", "REMIR_CLERK_PUBLISHABLE_KEY")
        // No references to script members in these lambdas: the configuration cache can't serialize them.
        variant.buildConfigFields?.put("API_URL", apiUrl.map { BuildConfigField("String", "\"$it\"", null) })
        variant.buildConfigFields?.put(
            "CLERK_PUBLISHABLE_KEY",
            clerkKey.map { BuildConfigField("String", "\"$it\"", null) },
        )
    }
}

dependencies {
    implementation(project(":core"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.core)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    implementation(platform(libs.ktor.bom))
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.resources)

    implementation(libs.jetbrains.markdown)

    implementation(libs.clerk.api)
    implementation(libs.clerk.ui)
}
