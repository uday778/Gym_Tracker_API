# ═══════════════════════════════════════════════════════════════════════════════
# Stage 1 — Build
# Uses the full Maven + JDK image to compile and package the fat JAR.
# This stage is discarded after build; it never ends up in the final image.
# ═══════════════════════════════════════════════════════════════════════════════
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /build

# ── Copy pom first to exploit Docker layer caching for dependency downloads ──
# As long as pom.xml doesn't change, `mvn dependency:go-offline` is cached.
COPY pom.xml .
RUN mvn dependency:go-offline -B --no-transfer-progress

# ── Copy source and build ────────────────────────────────────────────────────
COPY src ./src
RUN mvn package -DskipTests -B --no-transfer-progress

# ═══════════════════════════════════════════════════════════════════════════════
# Stage 2 — Runtime
# Copies only the compiled JAR into a minimal JRE image (~200 MB vs ~700 MB+).
# Smaller image = faster pulls, smaller attack surface, lower Render plan usage.
# ═══════════════════════════════════════════════════════════════════════════════
FROM eclipse-temurin:17-jre-alpine AS runtime

# ── Security: run as a non-root user ────────────────────────────────────────
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

WORKDIR /app

# ── Copy the fat JAR produced by Stage 1 ────────────────────────────────────
COPY --from=builder /build/target/gym-tracker-*.jar app.jar

# ── JVM tuning for containerised environments ────────────────────────────────
# -XX:+UseContainerSupport   → respect cgroup memory limits (not host RAM)
# -XX:MaxRAMPercentage=75.0  → use 75 % of container RAM for the JVM heap
# -XX:+UseG1GC               → low-latency GC appropriate for a web service
# -Dspring.profiles.active   → activate the docker profile if needed
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -Djava.security.egd=file:/dev/./urandom \
               -Dspring.profiles.active=default"

EXPOSE 8080

# Use shell form so that JAVA_OPTS is expanded by the shell
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
