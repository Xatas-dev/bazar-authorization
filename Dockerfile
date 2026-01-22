# We use Ubuntu Slim (matches the GitHub Runner's OS architecture)
# This ensures the binary built on the runner works inside the container.
FROM ubuntu:24.04-slim

WORKDIR /app

# Create a non-root user (security best practice)
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

# Copy the binary built by the GitHub Action
# NOTE: Verify the name matches your settings.gradle rootProject.name
COPY --chown=spring:spring build/native/nativeCompile/bazar-authorization /app/application

EXPOSE 8080

ENTRYPOINT ["/app/application"]