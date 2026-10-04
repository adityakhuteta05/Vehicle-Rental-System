# Multi-stage Dockerfile for DriveSense Platform
# Stage 1: Build the application using Maven
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /workspace

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy source code and build production uber-jar
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Minimal, secure JRE production runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root system user for security
RUN addgroup -S drivesense && adduser -S drivesense -G drivesense
USER drivesense:drivesense

# Copy built artifact from builder stage
COPY --from=builder --chown=drivesense:drivesense /workspace/target/drivesense-platform-1.0.0.jar /app/app.jar

# Configure cloud environment defaults
ENV PORT=8080 \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
