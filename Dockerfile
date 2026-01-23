FROM ubuntu:24.04

WORKDIR /app

# OPTIMIZATION: Install CA certificates (needed for HTTPS calls) and clean up apt cache to keep image small
RUN apt-get update && apt-get install -y ca-certificates && rm -rf /var/lib/apt/lists/*

# Create a non-root user (security best practice)
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

# Copy the binary
# Ensure this path matches your Gradle output
COPY --chown=spring:spring build/native/nativeCompile/bazar-authorization /app/application

EXPOSE 8080

ENTRYPOINT ["/app/application"]