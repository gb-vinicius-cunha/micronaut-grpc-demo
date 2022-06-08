import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.ofSourceSet
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.6.21"
    id("org.jetbrains.kotlin.kapt") version "1.6.21"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.6.21"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("io.micronaut.application") version "3.4.1"
    id("com.google.protobuf") version "0.8.18"

    id("info.solidsoft.pitest") version "1.7.4"
    id("org.jlleitschuh.gradle.ktlint") version "10.3.0"
    id("io.gitlab.arturbosch.detekt") version "1.20.0"
    id("org.jetbrains.kotlinx.kover") version "0.5.1"
}

version = "0.1"
group = "br.com.vroc.demo"

val kotlinVersion = project.properties["kotlinVersion"]
repositories {
    mavenCentral()
}

dependencies {
    // KOTLIN
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")

    // MICRONAUT
    implementation("io.micronaut:micronaut-jackson-databind")
    implementation("io.micronaut.grpc:micronaut-grpc-runtime")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("jakarta.annotation:jakarta.annotation-api")
    implementation("io.micronaut:micronaut-validation")
    implementation("io.micronaut:micronaut-management")
    implementation("io.micronaut:micronaut-aop")

    // MICROMETER
    implementation("io.micronaut.micrometer:micronaut-micrometer-core")
    implementation("io.micronaut:micronaut-management")
    implementation("io.micronaut.micrometer:micronaut-micrometer-registry-new-relic")
    implementation("io.micronaut.micrometer:micronaut-micrometer-registry-new-relic-telemetry")

    // TRACER
    implementation("io.micronaut:micronaut-tracing")
    implementation("io.micronaut.tracing:micronaut-tracing-zipkin")
    runtimeOnly("io.opentracing.contrib:opentracing-grpc:0.2.3")

    // GRPC
    implementation("io.grpc:grpc-kotlin-stub:1.3.0")
    implementation("io.grpc:grpc-services:1.47.0")

    // LOG
    runtimeOnly("ch.qos.logback:logback-classic:1.2.11")
    runtimeOnly("ch.qos.logback.contrib:logback-jackson:0.1.5")
    runtimeOnly("ch.qos.logback.contrib:logback-json-classic:0.1.5")

    // TEST
    testImplementation("io.micronaut:micronaut-http-client")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.25")
    testImplementation("io.mockk:mockk:1.12.4")
}

application {
    mainClass.set("br.com.vroc.demo.application.ApplicationKt")
}

java {
    sourceCompatibility = JavaVersion.toVersion("11")
}

sourceSets {
    main {
        java {
            srcDirs("build/generated/source/proto/main/grpc")
            srcDirs("build/generated/source/proto/main/grpckt")
            srcDirs("build/generated/source/proto/main/java")
        }
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.21.1"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.47.0"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:1.2.1:jdk7@jar"
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                // Apply the "grpc" plugin whose spec is defined above, without options.
                id("grpc")
                id("grpckt")
            }
        }
    }
}

micronaut {
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("br.com.vroc.demo.*")
    }
}

var generatedFiles = listOf(
    "*.grpc.*"
)

var excludeFiles = listOf(
    "*.application.Application*",
    "*.application.exceptions.*",
    "*.application.endpoints.extensions.*",
    "*.domain.model.*",
) + generatedFiles

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "11"
        }
    }

    compileTestKotlin {
        kotlinOptions {
            jvmTarget = "11"
        }
    }

    test {
        useJUnitPlatform()

        testLogging {
            events(FAILED, STANDARD_ERROR, SKIPPED)
            exceptionFormat = FULL
            showExceptions = true
            showCauses = true
            showStackTraces = true
        }

        environment["NEW_RELIC_ACCOUNT_ID"] = "123"
        environment["NEW_RELIC_LICENSE_KEY"] = "123"
    }

    koverMergedHtmlReport {
        excludes = excludeFiles
    }

    koverMergedXmlReport {
        excludes = excludeFiles
    }

    koverMergedVerify {
        excludes = excludeFiles

        rule {
            name = "Minimal line coverage rate in percent"
            bound {
                minValue = 90
            }
        }
    }

    withType<Test> {
        if (name == "testNativeImage") {
            extensions.configure(kotlinx.kover.api.KoverTaskExtension::class) {
                isDisabled = true
            }
        }
    }

    withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
        archiveBaseName.set("demo-grpc")
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "br.com.vroc.demo.application.ApplicationKt"))
        }
    }
}

detekt {
    config = files("detekt-config.yml")
}

ktlint {
    verbose.set(true)
    filter {
        exclude(generatedFiles)
        exclude { element -> element.file.path.contains("generated/") }
    }
}

pitest {
    junit5PluginVersion.set("0.15")
    timestampedReports.set(false)
    excludedClasses.set(excludeFiles)
    avoidCallsTo.set(listOf("kotlin.ResultKt", "kotlin.jvm.internal", "org.slf4j"))
}
