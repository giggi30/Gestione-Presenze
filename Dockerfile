# Build stage
FROM eclipse-temurin:17 AS build
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (cached layer)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code and build
COPY src src
RUN ./mvnw package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:17
WORKDIR /app

# Create non-root user for security (Debian-compatible)
RUN groupadd --system spring || true \
    && useradd --system --no-create-home --shell /bin/false --gid spring spring || true

# Copy JAR from build stage as root, set ownership and permissions
COPY --from=build /app/target/*.jar app.jar
RUN chown spring:spring /app/app.jar && chmod 755 /app/app.jar

# Switch to non-root user
USER spring:spring

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
