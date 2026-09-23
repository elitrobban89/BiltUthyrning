# ── Build stage ──────────────────────────────────────────
# Java 27 (GA 2026-09-15). Temurin har annu inga 27-avbildningar publicerade
# (eclipse-temurin:27-jdk/jre och maven:3.9-eclipse-temurin-27 ger alla 404 pa Docker Hub,
# kontrollerat 2026-09-22), sa bygget gar pa Liberica - samma OpenJDK 27, annan leverantor.
# Maven kommer fran wrappern i repot eftersom ingen maven-avbildning har JDK 27 an; den
# laddar ner sig sjalv med wget, curl ELLER bara java och staller darfor inga krav pa
# basavbildningen. Byt tillbaka till Temurin nar deras 27 dyker upp - tva rader.
FROM bellsoft/liberica-openjdk-debian:27 AS build
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
FROM bellsoft/liberica-openjre-alpine:27
# Liberica levererar INTE JDK:ns CDS-arkiv (lib/server/classes.jsa) som Temurin gjorde, sa
# varje JDK-klass laddades kallt. Pa Renders gratis-CPU tog kontexten da 28 s och Render gav
# upp portskanningen innan Tomcat lyssnade ("No open ports detected" -> Timed Out, 2026-09-22).
# -Xshare:dump bygger arkivet en gang har; TieredStopAtLevel=1 (bara C1) kortar starten
# ytterligare pa en CPU-snal instans.
RUN java -Xshare:dump
WORKDIR /app
COPY --from=build /app/target/car-rental-1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:TieredStopAtLevel=1", "-jar", "app.jar", "--spring.profiles.active=prod"]
