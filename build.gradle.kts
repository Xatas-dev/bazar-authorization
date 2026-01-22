import com.google.protobuf.gradle.id

val nativeImageConfigPath = "$projectDir/src/main/resources/META-INF/native-image"
val nativeImageAccessFilterConfigPath = "./src/test/resources/native/access-filter.json"

plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    id("org.springframework.boot") version "4.0.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.google.protobuf") version "0.9.5"
    kotlin("plugin.jpa") version "2.2.21"
    id("org.graalvm.buildtools.native") version "0.11.1"
}
val springGrpcVersion by extra("1.0.0")
group = "org.bazar"
version = "1.0.0"
description = "bazar-authorization"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

graalvmNative {
    binaries.all {
        buildArgs.add("--initialize-at-build-time=org.slf4j")
        buildArgs.add("--initialize-at-build-time=ch.qos.logback")
        buildArgs.add("--initialize-at-run-time=io.grpc.netty.shaded.io.netty.handler.ssl.BouncyCastleAlpnSslUtils")
        buildArgs.add("-H:+UnlockExperimentalVMOptions")
        buildArgs.add("-H:Preserve=package=liquibase.*")
        buildArgs.add("-march=native")
    }
}

repositories {
    mavenCentral()
}

val mockitoAgent = configurations.create("mockitoAgent")

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    // Mockito agent fix
    testImplementation("org.mockito:mockito-core:5.20.0")
    mockitoAgent("org.mockito:mockito-core:5.20.0") { isTransitive = false }
    //Observability
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    //gRPC
    implementation("io.grpc:grpc-services")
    implementation("io.grpc:grpc-kotlin-stub")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.springframework.grpc:spring-grpc-spring-boot-starter")
    //Cerbos
    implementation("dev.cerbos:cerbos-sdk-java:0.16.0")
    //Framework
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    //Security
    implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")

    //DB
    implementation("org.springframework.boot:spring-boot-starter-liquibase")
    runtimeOnly("org.postgresql:postgresql")

    //Test
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.grpc:spring-grpc-test")
    testImplementation("org.testcontainers:postgresql:1.21.0")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.boot:spring-boot-data-jpa-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.grpc:spring-grpc-dependencies:${property("springGrpcVersion")}")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks.compileKotlin {
    kotlinDaemonJvmArguments.add("-Xmx4096m")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:1.5.0:jdk8@jar"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc") {
                    option("@generated=omit")
                }
                id("grpckt") {
                    outputSubDir = "kotlin"
                }
            }
        }
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs()
    jvmArgs = listOf(
        "-javaagent:${mockitoAgent.asPath}", "-Xshare:off"
    )
}
