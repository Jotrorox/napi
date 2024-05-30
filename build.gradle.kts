plugins {
    kotlin("jvm") version "1.9.23"
    application
}

group = "com.jotrorox"
version = "1.1"

repositories {
    mavenCentral()
}

val exposedVersion = "0.50.1"

dependencies {
    testImplementation(kotlin("test"))

    // JSON Support
    implementation("com.google.code.gson:gson:2.11.0")

    // Toml Support
    implementation("com.akuleshov7:ktoml-core:0.5.1")
    implementation("com.akuleshov7:ktoml-file:0.5.1")

    // Ini support
    implementation("org.ini4j:ini4j:0.5.4")

    // Database support
    implementation("org.xerial:sqlite-jdbc:3.44.1.0")
    implementation("org.jetbrains.exposed", "exposed-core", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-dao", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-jdbc", exposedVersion)

    // Better Command Line features
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.6")
}

application {
    mainClass.set("com.jotrorox.napi.MainKt")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "com.jotrorox.napi.MainKt"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}