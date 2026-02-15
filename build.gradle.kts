import com.google.protobuf.gradle.id

plugins {
    kotlin("jvm") version "2.3.0"
    id("io.ktor.plugin") version "3.4.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.0"
    id("com.google.protobuf") version "0.9.5" // For gRPC
}

group = "org.bazar"
version = "1.0.1"

val exposedVersion = "1.0.0"
val logbackVersion = "1.5.13"
val hikariCpVersion = "7.0.2"
val postgresqlVersion = "42.7.9"
val micrometerRegistryVersion = "1.16.3"
val koinVersion = "4.1.1"
val grpcKotlinStubVersion = "1.5.0"
val grpcNettyVersion = "1.79.0"
val protobufVersion = "1.79.0"
val protobufKotlinVersion = "4.33.4"
val protocVersion = "4.33.4"
val grpcJavaPluginVersion = "1.79.0"
val grpcKotlinPluginVersion = "1.5.0"
val hopliteVersion = "2.9.0"
val configYamlVersion = "2.3.7"
val cerbosSdkVersion = "0.16.0"
val testContainersVersion = "2.0.3"
val testContainersPostgresqlVersion = "1.21.3"
val liquibaseTestVersion = "5.0.1"

application {
    mainClass = "org.bazar.authorization.BazarAuthorizationApplicationKt"
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    // Ktor Core
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-server-auth-jwt-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.ktor:ktor-server-metrics-micrometer-jvm")
    implementation("io.ktor:ktor-server-config-yaml:${configYamlVersion}")
    // Logging
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    // Database
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:${exposedVersion}")
    implementation("com.zaxxer:HikariCP:$hikariCpVersion")
    implementation("org.postgresql:postgresql:$postgresqlVersion")

    // Monitoring (Actuator equivalent)
    implementation("io.micrometer:micrometer-registry-prometheus:$micrometerRegistryVersion")

    // DI
    implementation("io.insert-koin:koin-ktor:$koinVersion")
    implementation("io.insert-koin:koin-logger-slf4j:$koinVersion")

    // gRPC
    implementation("io.grpc:grpc-kotlin-stub:$grpcKotlinStubVersion")
    implementation("io.grpc:grpc-netty-shaded:$grpcNettyVersion") // High performance shaded netty
    implementation("io.grpc:grpc-protobuf:$protobufVersion")
    implementation("io.grpc:grpc-stub:$protobufVersion")
    implementation("com.google.protobuf:protobuf-kotlin:$protobufKotlinVersion")
    implementation("io.grpc:grpc-services:$protobufVersion")

    //Cerbos
    implementation("dev.cerbos:cerbos-sdk-java:$cerbosSdkVersion")

    //Test
    testImplementation("org.testcontainers:testcontainers:$testContainersVersion")
    testImplementation("org.testcontainers:postgresql:$testContainersPostgresqlVersion")
    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("io.insert-koin:koin-test:$koinVersion")
    testImplementation("io.insert-koin:koin-test-junit5:$koinVersion")
    testImplementation("org.testcontainers:junit-jupiter:1.20.4")
    testImplementation("io.grpc:grpc-testing:$grpcNettyVersion")
    testImplementation("io.grpc:grpc-inprocess:$grpcNettyVersion")
    testImplementation("org.assertj:assertj-core:3.27.3")
    implementation("org.liquibase:liquibase-core:$liquibaseTestVersion")
}

protobuf {
    protoc { artifact = "com.google.protobuf:protoc:$protocVersion" }
    plugins {
        id("grpc") { artifact = "io.grpc:protoc-gen-grpc-java:$grpcJavaPluginVersion" }
        id("grpckt") { artifact = "io.grpc:protoc-gen-grpc-kotlin:$grpcKotlinPluginVersion:jdk8@jar" }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc")
                id("grpckt")
            }
            it.builtins {
                id("kotlin")
            }
        }
    }
}

tasks.compileKotlin {
    kotlinDaemonJvmArguments.add("-Xmx4096m")
}

tasks.test {
    useJUnitPlatform()
}