# ── Build stage ──────────────────────────────────────────
# Java 27 pa Eclipse Temurin. Bygget gick 2026-09-22--10-05 pa Liberica eftersom Temurin
# saknade 27-avbildningar; eclipse-temurin:27-jdk/-jre/-jre-alpine finns nu (kontrollerat 2026-10-05).
# Maven kommer fortfarande fran wrappern i repot: maven:3.9-eclipse-temurin-27 finns inte an,
# och wrappern hamtar Maven sjalv med wget, curl ELLER bara java.
FROM eclipse-temurin:27-jdk AS build
WORKDIR /app

# Cache dependencies
COPY pom.xml .
COPY .mvn ./.mvn
COPY mvnw .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -P web -q

# Build without JavaFX (-P web excludes org.openjfx from fat JAR)
COPY src ./src
RUN ./mvnw clean package -P web -DskipTests -q

# ── Runtime stage ─────────────────────────────────────────
FROM eclipse-temurin:27-jre-alpine
# Behalls efter bytet till Temurin (ofarligt om arkivet redan finns; startmarginalen pa Render
# ar for liten for att chansa). Bakgrund: Liberica levererade INTE JDK:ns CDS-arkiv (lib/server/classes.jsa) som Temurin gjorde, sa
# varje JDK-klass laddades kallt. Pa Renders gratis-CPU tog kontexten da 28 s och Render gav
# upp portskanningen innan Tomcat lyssnade ("No open ports detected" -> Timed Out, 2026-09-22).
# -Xshare:dump bygger arkivet en gang har; TieredStopAtLevel=1 (bara C1) kortar starten
# ytterligare pa en CPU-snal instans.
RUN java -Xshare:dump
WORKDIR /app
COPY --from=build /app/target/car-rental-1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:TieredStopAtLevel=1", "-jar", "app.jar", "--spring.profiles.active=prod"]
