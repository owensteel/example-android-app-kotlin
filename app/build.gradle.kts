import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    alias(libs.plugins.google.ksp)
    alias(libs.plugins.google.hilt)
}

// Get secret keys from local properties
val localProperties = Properties()
val localFile = rootProject.file("local.properties")
if (localFile.exists()) {
    localFile.inputStream().use { localProperties.load(it) }
}
val clientId = localProperties["starling.clientId"] as String? ?: ""
val clientSecret = localProperties["starling.clientSecret"] as String? ?: ""
val initRefreshToken = localProperties["refresh.token"] as String? ?: ""

android {
    namespace = "com.owensteel.starlingroundup"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.owensteel.starlingroundup"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "dagger.hilt.android.testing.HiltTestRunner"

        // Inject secret keys into BuildConfig
        buildConfigField("String", "CLIENT_ID", "\"$clientId\"")
        buildConfigField("String", "CLIENT_SECRET", "\"$clientSecret\"")
        buildConfigField("String", "INIT_REFRESH_TOKEN", "\"$initRefreshToken\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

val mockitoAgent = configurations.create("mockitoAgent")
dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Retrofit + JSON converter
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // Kotlin Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Extra Compose setup
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Security
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.security.crypto)
    implementation(libs.rootbeer.lib)

    // Compose navigation
    implementation(libs.androidx.navigation.compose)

    // DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // Hilt + Compose integration
    implementation(libs.androidx.hilt.navigation.compose)

    // Testing
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.mockito.inline)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.mockk.v1138)
    androidTestImplementation(libs.hilt.android.testing)
    androidTestAnnotationProcessor(libs.hilt.compiler)
    androidTestImplementation(libs.mockito.android)
    androidTestImplementation(libs.mockito.kotlin)

    mockitoAgent(libs.mockito.core) { isTransitive = false }
}
tasks.withType<Test>().configureEach {
    jvmArgs("-javaagent:${mockitoAgent.asPath}")
}