# ==========================================
# Stage 1: Build the Application
# ==========================================
FROM gradle:9.2.1-jdk25 AS builder

WORKDIR /app

# Copy gradle configuration first to cache dependencies
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle

# Download dependencies (this layer will be cached unless dependencies change)
# 'no-daemon' prevents Gradle from hanging in the background
RUN gradle clean build -x test --no-daemon --quiet || return 0

# Copy source code and build the final jar
COPY src ./src
RUN gradle bootJar -x test --no-daemon

# Extract layers for optimization
# This splits the fat jar into dependencies, loader, and application code
RUN mv build/libs/bazar-authorization-*.jar build/libs/application.jar
WORKDIR /app/build/libs
RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted

# ==========================================
# Stage 2: Create the Runtime Image
# ==========================================
FROM eclipse-temurin:25-jre-alpine

WORKDIR /application

# Optimize Java memory usage for containers
# MaxRAMPercentage=75.0 means the JVM will use 75% of the container's available memory limit (e.g., 384MB of a 512MB container)
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseStringDeduplication -Xmx80M"

# Create a non-root user for security (best practice)
#RUN addgroup -S spring && adduser -S spring -G spring && chown -R spring:spring /application
#USER spring:spring

# Copy the layers extracted in Stage 1
# Order matters: dependencies are least likely to change, application is most likely
COPY --from=builder /app/build/libs/extracted/dependencies/ ./
COPY --from=builder /app/build/libs/extracted/spring-boot-loader/ ./
COPY --from=builder /app/build/libs/extracted/snapshot-dependencies/ ./
COPY --from=builder /app/build/libs/extracted/application/ ./


ENTRYPOINT ["java", "-jar", "application.jar"]