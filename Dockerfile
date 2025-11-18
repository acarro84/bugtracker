# =========================
# 1) BUILD IMAGE
# =========================
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build

# Copy pom first for layer caching
COPY pom.xml .
# Pre-fetch dependencies (optional; speeds up subsequent builds)
RUN mvn -q -e -DskipTests dependency:go-offline

# Copy source and build
COPY src ./src

RUN mvn -q -DskipTests clean package


# =========================
# 2) RUNTIME IMAGE
# =========================
FROM eclipse-temurin:17-jre-jammy

# Create a non-root user
RUN useradd -u 1001 -m appuser

# App working dir
WORKDIR /app

# Directories for uploads / reports (we will mount volumes here)
RUN mkdir -p /uploads /reports \
    && chown -R appuser:appuser /uploads /reports /app

# Copy the fat JAR from the build stage
COPY --from=build /build/target/bugtracker-0.0.1-SNAPSHOT.jar /app/app.jar

# Switch to non-root
USER appuser

# Expose app port
EXPOSE 8080

# Default profile for containers; you can override in docker-compose
ENV SPRING_PROFILES_ACTIVE=docker

# Allow tuning JAVA_OPTS (heap, GC, etc.) from the environment
ENV JAVA_OPTS=""

# Entrypoint
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
