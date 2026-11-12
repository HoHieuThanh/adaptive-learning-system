# ==============================================================================
# MULTI-STAGE DOCKERFILE CHO SPRING BOOT 3 (JAVA 21)
# Stage 1: Build JAR bằng Maven
# Stage 2: Runtime với Eclipse Temurin JRE 21 Alpine nhẹ
# ==============================================================================

# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests -B

# Stage 2: Minimal Runtime
FROM eclipse-temurin:21-jre-alpine

# Cài đặt curl phục vụ container healthcheck
RUN apk add --no-cache curl

WORKDIR /app

# Non-root user để tăng cường an toàn
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /app/target/*.jar app.jar
RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=5 \
  CMD curl -f http://127.0.0.1:8080/api/health || exit 1

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
