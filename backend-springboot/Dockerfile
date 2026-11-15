# =============================================================================
# CIVICBUILD SPRING BOOT BACKEND - MULTI-STAGE DOCKERFILE
# Java 17 / Eclipse Temurin Alpine Production Container
# =============================================================================

# Stage 1: Build & Package
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /build

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Compile source code and BPMN models
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Minimal Distroless / JRE Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root application user for container security
RUN addgroup -S civicgroup && adduser -S civicuser -G civicgroup

# Create persistent upload and permit output directories
RUN mkdir -p /app/uploads /app/generated-permits && \
    chown -R civicuser:civicgroup /app

# Copy executable jar from builder
COPY --from=builder /build/target/civicbuild-backend-1.0.0.jar /app/app.jar

USER civicuser

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=postgres \
    JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/api/departments || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
