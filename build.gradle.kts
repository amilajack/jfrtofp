
group = "me.bechberger"
description = "Converting JFR files to Firefox Profiler profiles"

inner class ProjectInfo {
    val longName = "JFR to Firefox Profiler converter"
    val website = "https://github.com/parttimenerd/jfrtofp"
    val scm = "git@github.com:parttimenerd/$name.git"
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
    gradlePluginPortal()
}

plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"

    id("com.github.johnrengelman.shadow") version "7.1.2"

    id("io.gitlab.arturbosch.detekt") version "1.21.0"
    pmd

    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"

    `maven-publish`

    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

pmd {
    isConsoleOutput = true
    toolVersion = "6.21.0"
    rulesMinimumPriority.set(5)
    ruleSets = listOf("category/java/errorprone.xml", "category/java/bestpractices.xml")
}

apply { plugin("com.github.johnrengelman.shadow") }

detekt {
    buildUponDefaultConfig = true // preconfigure defaults
    config = files("$rootDir/config/detekt/detekt.yml")
    autoCorrect = true
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    jvmTarget = "1.8"
}
tasks.withType<io.gitlab.arturbosch.detekt.DetektCreateBaselineTask>().configureEach {
    jvmTarget = "1.11"
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // This dependency is used by the application.
    implementation("org.junit.jupiter:junit-jupiter:5.8.1")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.4.0-RC")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.21.0")
    implementation("info.picocli:picocli:4.6.3")
}

tasks.test {
    useJUnitPlatform()
}

application {
    // Define the main class for the application.
    mainClass.set("me.bechberger.jfrtofp.MainKt")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.register<Copy>("copyHooks") {
    from("bin/pre-commit")
    into(".git/hooks")
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/parttimenerd/jfrtofp")
            description = project.description
            version = project.version
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }
    publications {
        register<MavenPublication>("gpr") {
            from(components["java"])
        }
    }
}
