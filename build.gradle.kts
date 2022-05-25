val kotlinVersion: String by project
val exposedVersion: String by project
val ktorVersion: String by project
val myndocsOauthVersion: String by project

plugins {
    kotlin("jvm") version "1.5.30"
    id("org.jlleitschuh.gradle.ktlint") version "10.1.0"
    application
}

group = "uk.co.thomasc"
version = "1.0-SNAPSHOT"

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(15))
    }
    sourceSets.all {
        languageSettings.optIn("kotlin.io.path.ExperimentalPathApi")
        languageSettings.optIn("io.ktor.locations.KtorExperimentalLocationsAPI")
        languageSettings.optIn("kotlin.time.ExperimentalTime")
        languageSettings.optIn("io.ktor.util.KtorExperimentalAPI")
        languageSettings.optIn("kotlin.ExperimentalUnsignedTypes")
    }
}

dependencies {
    repositories {
        mavenCentral()
    }

    implementation("io.ktor:ktor-jackson:$ktorVersion")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.6.1")

    implementation("io.ktor:ktor-mustache:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-html-builder:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.3")
    implementation("io.ktor:ktor-client-apache:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("io.ktor:ktor-locations:$ktorVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.5.2")

    implementation("com.luckycatlabs:SunriseSunsetCalculator:1.2")

    // DB & Migrations
    implementation("mysql:mysql-connector-java:8.0.29")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation("org.flywaydb:flyway-core:7.14.0")
    implementation("com.zaxxer:HikariCP:3.4.2")

    testImplementation("org.junit.platform:junit-platform-launcher:1.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.0.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.1.50")

    implementation("com.ToxicBakery.library.bcrypt:bcrypt:+")

    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.1.0")

    implementation("nl.myndocs:oauth2-server-core:$myndocsOauthVersion")
    implementation("nl.myndocs:oauth2-server-ktor:$myndocsOauthVersion")
    // In memory dependencies
    implementation("nl.myndocs:oauth2-server-client-inmemory:$myndocsOauthVersion")
    implementation("nl.myndocs:oauth2-server-identity-inmemory:$myndocsOauthVersion")
    implementation("nl.myndocs:oauth2-server-token-store-inmemory:$myndocsOauthVersion")

    implementation("io.github.microutils:kotlin-logging:2.1.14")
}

application {
    mainClass.set("uk.co.thomasc.thealley.MainKt")
}