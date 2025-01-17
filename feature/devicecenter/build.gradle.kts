import mega.privacy.android.build.preBuiltSdkDependency
import mega.privacy.android.build.shouldApplyDefaultConfiguration

plugins {
    alias(convention.plugins.mega.android.library)
    alias(convention.plugins.mega.android.test)
    id("kotlin-android")
    id("kotlin-kapt")
    id("de.mannodermaus.android-junit5")
}

android {
    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = androidx.versions.compose.compiler.get()
    }

    kotlin {
        val jdk: String by rootProject.extra
        jvmToolchain(jdk.toInt())
    }

    kotlinOptions {
        val jdk: String by rootProject.extra
        jvmTarget = jdk
        val shouldSuppressWarnings: Boolean by rootProject.extra
        suppressWarnings = shouldSuppressWarnings
        freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
    }

    lint {
        abortOnError = false
        xmlOutput = file("build/reports/lint-results.xml")
    }
    namespace = "mega.privacy.android.feature.devicecenter"
}

dependencies {
    preBuiltSdkDependency(rootProject.extra)

    implementation(project(":core:formatter"))
    lintChecks(project(":lint"))

    implementation(project(":analytics"))
    implementation(project(":domain"))
    implementation(project(":navigation"))
    implementation(project(":data"))
    implementation(project(":core-ui"))
    implementation(project(":shared:theme"))
    implementation(project(":shared:sync"))
    implementation(project(":shared:resources"))
    implementation(project(":legacy-core-ui"))
    implementation(project(":icon-pack"))

    testImplementation(project(":core-test"))
    testImplementation(project(":core-ui-test"))

    implementation(lib.mega.analytics)
    implementation(lib.kotlin.ktx)
    implementation(lib.logging.timber)
    implementation(androidx.appcompat)
    implementation(androidx.fragment)
    implementation(google.material)
    implementation(androidx.lifecycle.viewmodel)
    implementation(androidx.lifecycle.service)
    implementation(google.hilt.android)

    if (shouldApplyDefaultConfiguration(project)) {
        apply(plugin = "dagger.hilt.android.plugin")

        kapt(google.hilt.android.compiler)
        kapt(androidx.hilt.compiler)
    }

    // Compose
    implementation(androidx.lifecycle.runtime.compose)
    implementation(androidx.compose.activity)
    implementation(androidx.compose.viewmodel)
    implementation(platform(androidx.compose.bom))
    implementation(androidx.bundles.compose.bom)
    implementation(lib.compose.state.events)
    implementation(androidx.hilt.navigation)
    implementation(androidx.constraintlayout.compose)
    implementation(lib.compose.state.events)

    testImplementation(testlib.compose.junit)
    testImplementation(testlib.bundles.ui.test)
    testImplementation(testlib.bundles.unit.test)

    testRuntimeOnly(testlib.junit.jupiter.engine)
    testImplementation(testlib.bundles.junit5.api)
}