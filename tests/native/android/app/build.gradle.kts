plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

dependencyLocking {
    lockAllConfigurations()
    lockMode.set(LockMode.STRICT)
}

configurations.configureEach {
    // Build tooling libraries moved to releases that fix their published security advisories.
    resolutionStrategy.eachDependency {
        val patched = mapOf(
            "io.netty" to "4.1.141.Final", "org.bouncycastle" to "1.87", "org.apache.commons:commons-compress" to "1.28.0",
            "org.bitbucket.b_c:jose4j" to "0.9.7", "org.jdom:jdom2" to "2.0.6.1", "com.google.protobuf:protobuf-java" to "3.25.9",
            "com.google.protobuf:protobuf-java-util" to "3.25.9", "com.google.protobuf:protobuf-kotlin" to "3.25.9",
        )
        val version = patched["${requested.group}:${requested.name}"] ?: patched[requested.group]?.takeUnless { requested.name.startsWith("netty-tcnative") }
        if (version != null) useVersion(version)
        if (requested.group == "com.google.guava" && requested.name == "guava" && requested.version?.endsWith("-jre") == true) useVersion("33.7.2-jre")
    }
}

android {
    namespace = "app.example.media"
    compileSdk = 36
    defaultConfig {
        applicationId = "app.example.media"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin { jvmToolchain(17) }
    testOptions.unitTests.isReturnDefaultValues = true
    sourceSets.getByName("main").java.srcDir("../../../../implementation/android/src/main/java")
}

dependencies {
    implementation("io.livekit:livekit-android:2.29.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    testImplementation("org.mockito:mockito-core:5.20.0")
}
