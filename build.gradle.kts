import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

val kotlinVersion: String by project
val exposedVersion: String by project
val ktorVersion: String by project
val myndocsOauthVersion: String by project

plugins {
    kotlin("jvm") version "1.9.20"
    kotlin("plugin.serialization") version "1.9.20"
    id("org.jlleitschuh.gradle.ktlint") version "11.5.1"
    application
}

group = "uk.co.thomasc"

kotlin {
    jvmToolchain {
        this.languageVersion.set(JavaLanguageVersion.of(16))
    }
    sourceSets.all {
        languageSettings.optIn("kotlin.io.path.ExperimentalPathApi")
        languageSettings.optIn("io.ktor.server.locations.KtorExperimentalLocationsAPI")
        languageSettings.optIn("kotlin.time.ExperimentalTime")
        languageSettings.optIn("kotlin.ExperimentalUnsignedTypes")
        languageSettings.optIn("kotlinx.coroutines.FlowPreview")
        languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
    }
}

dependencies {
    repositories {
        mavenCentral()
        maven { url = uri("https://artifactory.kirkstall.top-cat.me") }
    }

    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-mustache:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-html-builder:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.8.0")
    implementation("io.ktor:ktor-client-apache:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:1.4.7")
    implementation("io.ktor:ktor-server-locations:$ktorVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.7.3")

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
    implementation("com.luckycatlabs:SunriseSunsetCalculator:1.2")
    implementation("at.topc.tado:tado-kt:1.0.10")

    // DB & Migrations
    implementation("mysql:mysql-connector-java:8.0.33")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation("org.flywaydb:flyway-mysql:10.1.0")
    implementation("com.zaxxer:HikariCP:3.4.2")

    testImplementation("org.junit.platform:junit-platform-launcher:1.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.0.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.1.50")

    implementation("com.ToxicBakery.library.bcrypt:bcrypt:+")

    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")

    implementation("nl.myndocs:oauth2-server-core:$myndocsOauthVersion")
    implementation("nl.myndocs:oauth2-server-ktor:$myndocsOauthVersion")
    // In memory dependencies
    implementation("nl.myndocs:oauth2-server-client-inmemory:$myndocsOauthVersion")
    implementation("nl.myndocs:oauth2-server-identity-inmemory:$myndocsOauthVersion")
    implementation("nl.myndocs:oauth2-server-token-store-inmemory:$myndocsOauthVersion")

    implementation("io.github.microutils:kotlin-logging:3.0.5")
}

tasks.test {
    useJUnitPlatform()
}

ktlint {
    version.set("0.50.0")
    reporters {
        reporter(ReporterType.CHECKSTYLE)
    }
}

application {
    mainClass.set("uk.co.thomasc.thealley.MainKt")
}
