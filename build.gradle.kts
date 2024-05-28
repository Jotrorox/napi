plugins {
    kotlin("jvm") version "1.9.23"
}

group = "com.jotrorox"
version = "1.0"

repositories {
    mavenCentral()
}

val exposedVersion = "0.50.1"

dependencies {
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("org.xerial:sqlite-jdbc:3.44.1.0")
    implementation("org.jetbrains.exposed", "exposed-core", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-dao", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-jdbc", exposedVersion)
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.6")
}

kotlin {
    jvmToolchain(21)
}