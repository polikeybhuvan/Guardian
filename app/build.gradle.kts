plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
}

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  val localDebugKeystore = rootProject.file("debug.keystore")

  defaultConfig {
    applicationId = "com.aistudio.sosmesh.kxmbpv"
    minSdk = 24
    targetSdk = 36
    versionCode = 4
    versionName = "1.0.4"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  // Keep private release signing material outside the repository.
  signingConfigs {
    // Reuse an existing ignored local debug key when available, so Studio can
    // update an app already installed on this developer's device. Fresh clones
    // without this file continue to use Android's generated debug key.
    if (localDebugKeystore.exists()) {
      create("localDebug") {
        storeFile = localDebugKeystore
        storePassword = "android"
        keyAlias = "androiddebugkey"
        keyPassword = "android"
      }
    }

    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
    }
  }

  buildTypes {
    debug {
      if (localDebugKeystore.exists()) {
        signingConfig = signingConfigs.getByName("localDebug")
      }
    }

    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      // Release signing credentials are provided by the person building a release APK.
      signingConfig = signingConfigs.getByName("release")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  // The map and Gemma weights are streamed into app-private storage before use.
  // Keep them compressed in the APK to avoid exceeding Android package size limits.
  testOptions { unitTests { isIncludeAndroidResources = true } }
  dependenciesInfo {
    includeInApk = false
    includeInBundle = true
  }
}

dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.moshi.kotlin)
  implementation(libs.play.services.location)
  implementation(libs.play.services.nearby)
  implementation(libs.mapsforge.map.android)
  implementation(libs.mapsforge.themes)
  implementation(libs.litertlm.android)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}
