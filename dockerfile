# Build stage
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Create app user
RUN useradd -r -g users -d /app -s /sbin/nologin appuser
RUN chown -R appuser:users /app

# Copy the application
COPY --from=build --chown=appuser:users /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Healthcheck
HEALTHCHECK --interval=30s --timeout=3s \
  CMD curl -f http://localhost:8080/api/v1/actuator/health || exit 1

# Run as non-root user
USER appuser

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]