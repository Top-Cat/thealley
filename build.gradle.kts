import io.miret.etienne.gradle.sass.CompileSass
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

val kotlinVersion: String by project
val exposedVersion: String by project
val ktorVersion: String by project
val myndocsOauthVersion: String by project

plugins {
    kotlin("multiplatform") version "1.9.20"
    kotlin("plugin.serialization") version "1.9.20"
    id("io.miret.etienne.sass") version "1.1.2"
    id("org.jlleitschuh.gradle.ktlint") version "11.5.1"
    application
}

group = "uk.co.thomasc"

repositories {
    mavenCentral()
    maven { url = uri("https://artifactory.kirkstall.top-cat.me") }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
        withJava()
    }
    js(IR) {
        browser {
            binaries.executable()
            webpackTask {
                cssSupport {
                    enabled.set(true)
                }
            }
            runTask {
                cssSupport {
                    enabled.set(true)
                }
            }
            testTask {
                useKarma {
                    useChromeHeadless()
                    webpackConfig.cssSupport {
                        enabled.set(true)
                    }
                }
            }
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmMain by getting {
            with(languageSettings) {
                optIn("kotlin.io.path.ExperimentalPathApi")
                optIn("io.ktor.server.locations.KtorExperimentalLocationsAPI")
                optIn("kotlin.time.ExperimentalTime")
                optIn("kotlin.ExperimentalUnsignedTypes")
                optIn("kotlinx.coroutines.FlowPreview")
                optIn("kotlinx.coroutines.DelicateCoroutinesApi")
            }

            dependencies {
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                implementation("io.github.pdvrieze.xmlutil:serialization:0.86.2")

                implementation("io.ktor:ktor-server-auth:$ktorVersion")
                implementation("io.ktor:ktor-server-mustache:$ktorVersion")
                implementation("io.ktor:ktor-server-netty:$ktorVersion")
                implementation("io.ktor:ktor-server-html-builder:$ktorVersion")
                implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.8.0")
                implementation("io.ktor:ktor-client-apache:$ktorVersion")
                implementation("io.ktor:ktor-client-cio:$ktorVersion")
                implementation("ch.qos.logback:logback-classic:1.4.7")
                implementation("io.ktor:ktor-server-locations:$ktorVersion")
                implementation("io.ktor:ktor-client-websockets:$ktorVersion")

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.7.3")

                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.1")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
                implementation("com.luckycatlabs:SunriseSunsetCalculator:1.2")
                implementation("at.topc.tado:tado-kt:1.0.22")
                implementation("com.google.auth:google-auth-library-oauth2-http:1.19.0")

                // DB & Migrations
                implementation("mysql:mysql-connector-java:8.0.33")

                implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
                implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
                implementation("org.flywaydb:flyway-mysql:10.1.0")
                implementation("com.zaxxer:HikariCP:3.4.2")

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
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit5"))

                implementation("org.junit.jupiter:junit-jupiter-api:5.0.0")
                implementation("org.junit.jupiter:junit-jupiter-engine:5.0.0")
            }
        }
        val jsMain by getting {
            with(languageSettings) {
                optIn("kotlin.js.ExperimentalJsExport")
                optIn("kotlin.time.ExperimentalTime")
                optIn("kotlinx.serialization.ExperimentalSerializationApi")
                optIn("kotlin.io.encoding.ExperimentalEncodingApi")
            }
            dependencies {
                implementation("org.jetbrains.kotlin-wrappers:kotlin-extensions:1.0.1-pre.323-kotlin-1.6.10")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-legacy:17.0.2-pre.323-kotlin-1.6.10")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom-legacy:17.0.2-pre.323-kotlin-1.6.10")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-router-dom:6.2.2-pre.323-kotlin-1.6.10")
                implementation(npm("axios", "0.21.1"))
                implementation(npm("bootswatch", "5.1.3"))
                implementation(npm("bootstrap", "5.1.3"))
                implementation(devNpm("webpack-bundle-analyzer", "4.6.1"))
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
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

tasks.getByName<CompileSass>("compileSass") {
    dependsOn(tasks.getByName("kotlinNpmInstall"))

    outputDir = file("$buildDir/processedResources/jvm/main/static")
    setSourceDir(file("$projectDir/src/jvmMain/sass"))
    loadPath(file("$buildDir/js/node_modules"))

    @Suppress("INACCESSIBLE_TYPE")
    style = compressed
}

tasks.getByName<KotlinWebpack>("jsBrowserProductionWebpack") {
    mainOutputFileName.set("output.js")
    sourceMaps = true
    outputDirectory.set(file("$buildDir/processedResources/jvm/main/static"))
}

tasks.withType<AbstractCopyTask> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<AbstractArchiveTask> {
    isPreserveFileTimestamps = true
}

tasks.getByName<Jar>("jvmJar") {
    dependsOn(tasks.getByName("jsBrowserProductionWebpack"), tasks.getByName("compileSass"))
    val jsBrowserProductionWebpack = tasks.getByName<KotlinWebpack>("jsBrowserProductionWebpack")

    from(jsBrowserProductionWebpack.outputDirectory.get())
    listOf(jsBrowserProductionWebpack.mainOutputFileName.get(), jsBrowserProductionWebpack.mainOutputFileName.get() + ".map", "modules.js", "modules.js.map").forEach {
        from(File(jsBrowserProductionWebpack.outputDirectory.get().asFile, it))
    }
}

tasks.getByName<JavaExec>("run") {
    dependsOn(tasks.getByName<Jar>("jvmJar"))
    classpath(tasks.getByName<Jar>("jvmJar"))
}

distributions {
    main {
        contents {
            from("$buildDir/libs") {
                rename("${rootProject.name}-jvm", rootProject.name)
                into("lib")
            }
        }
    }
}
