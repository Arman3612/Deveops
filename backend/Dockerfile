# ============================================================
# Backend Dockerfile — Multi-stage Build
# Online Examination System (BUG)
# ============================================================

# ── Stage 1: Build (Maven) ──────────────────────────────────
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app

# Copy POM first for dependency caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and package
COPY src ./src
RUN mvn clean package -DskipTests

# ── Stage 2: Runtime (JRE) ──────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

LABEL maintainer="BUG Online Exam System"
LABEL description="Backend API for Online Examination System"

# Install curl for health checks
RUN apk add --no-cache curl

# Copy the built JAR
COPY --from=build /app/target/proctor-0.0.1-SNAPSHOT.jar app.jar

# Health check: verify Spring Boot is responding
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=5 \
    CMD curl -sf http://localhost:8080/api/health || exit 1

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
