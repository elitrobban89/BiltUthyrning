# ── Build stage ──────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -P web -q

# Build without JavaFX (-P web excludes org.openjfx from fat JAR)
COPY src ./src
RUN mvn clean package -P web -DskipTests -q

# ── Runtime stage ─────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/car-rental-1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
