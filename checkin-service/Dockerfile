# syntax=docker/dockerfile:1
#
# checkin-service — multi-stage image.
#   stage 1 (build)   : full JDK + Maven, produces the Spring Boot fat jar
#   stage 2 (runtime) : JRE only, non-root user, actuator-based HEALTHCHECK
#
# Build context is this service directory only; every service is an independent
# Maven project (its parent, spring-boot-starter-parent, comes from Maven Central).
#
#   docker build -t nightflow/checkin-service:local checkin-service

# ---------------------------------------------------------------- build stage
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build

# Dependencies are resolved before the sources are copied, so the (slow) layer
# is only rebuilt when pom.xml changes. The cache mount additionally shares the
# local Maven repository between builds of different services.
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn -B -ntp -DskipTests dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn -B -ntp -DskipTests package \
 && cp target/*.jar /build/app.jar

# -------------------------------------------------------------- runtime stage
FROM eclipse-temurin:25-jre AS runtime

# curl is needed by HEALTHCHECK; the unprivileged user is created here so the
# application never runs as root.
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl \
 && rm -rf /var/lib/apt/lists/* \
 && groupadd --system --gid 1001 nightflow \
 && useradd --system --uid 1001 --gid nightflow --home-dir /app --shell /usr/sbin/nologin nightflow

WORKDIR /app
COPY --from=build --chown=nightflow:nightflow /build/app.jar /app/app.jar

USER nightflow:nightflow

# SERVER_PORT is also Spring Boot's relaxed binding for `server.port`, so the
# port the container exposes and the port the app listens on cannot diverge.
ENV SERVER_PORT=8097 \
    JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

EXPOSE 8097

HEALTHCHECK --interval=15s --timeout=5s --start-period=90s --retries=20 \
  CMD curl -fsS "http://127.0.0.1:${SERVER_PORT}/actuator/health" || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
