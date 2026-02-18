# ==========================================
# Stage 1: Build the Application
# ==========================================
FROM gradle:9.2.1-jdk21 AS builder

WORKDIR /app

# 1. Cache Gradle dependencies<!--citation:1-->
# Copy only files needed for dependency resolution
COPY build.gradle.kts settings.gradle.kts ./
# If you have a separate gradle folder for the wrapper:
# COPY gradle ./gradle

# This triggers a download of dependencies without building the app
RUN gradle dependencies --no-daemon || true

# 2. Build the application
COPY src ./src
# installDist creates a directory with /bin and /lib (all jars)
RUN gradle installDist --no-daemon -x test

# ==========================================
# Stage 2: Create the Runtime Image
# ==========================================
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Optimize JVM for Ktor/Netty (High performance, low memory footprint)
# -XX:+UseZGC is excellent for low-latency gRPC services if using Java 21+
ENV JDK_JAVA_OPTIONS="-XX:MaxRAMPercentage=80.0 \
                      -XX:+UseStringDeduplication \
                      -XX:+ExitOnOutOfMemoryError \
                      -Xss512k"

RUN addgroup -S ktor && adduser -S ktor -G ktor
USER ktor

# 3. Layered Copying for Maximum Caching
# First, copy the dependencies (this layer is heavy but changes rarely)
# We assume project name is "bazar-authorization" (matches your package)
COPY --from=builder --chown=ktor:ktor /app/build/install/bazar-authorization/lib /app/lib

# Second, copy the startup scripts
COPY --from=builder --chown=ktor:ktor /app/build/install/bazar-authorization/bin /app/bin

# Third, copy the config files (if they aren't bundled in the JAR)
# COPY --chown=ktor:ktor src/main/resources/application.yaml /app/config/application.yaml

# Expose both Ktor (HTTP) and gRPC ports
EXPOSE 8080
EXPOSE 9090

# Use the generated shell script to start the app.
# It handles the classpath and main class automatically.
ENTRYPOINT ["./bin/bazar-authorization"]