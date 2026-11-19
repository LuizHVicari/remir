# syntax=docker/dockerfile:1
# Image of the Ktor server (Cloud Run). Build from the repo root: `docker build -t remir-server .`

FROM eclipse-temurin:21-jdk AS build
WORKDIR /src
COPY . .
# The Android app is not part of this image, and configuring it needs the Android SDK.
RUN sed -i 's/, ":app"//' settings.gradle.kts && rm -rf app
RUN ./gradlew :server:installDist --no-daemon -x test

FROM eclipse-temurin:21-jre
RUN useradd --system --uid 10001 remir
WORKDIR /app
COPY --from=build /src/server/build/install/server/ ./
USER remir
# Cloud Run sets PORT; the server reads it (application.conf).
ENV PORT=8080
EXPOSE 8080
# Fit the heap to the container's memory limit; SerialGC suits a small single-vCPU instance.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC"
ENTRYPOINT ["./bin/server"]
