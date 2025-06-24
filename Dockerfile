# stage 1: builder
FROM gradle:8.5.0-jdk17 AS builder
WORKDIR /workspace
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew clean bootJar --no-daemon

# stage 2: runtime
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=builder /workspace/build/libs/*.jar app.jar

# 환경변수는 런타임에 주입되어야 함 (보안상 기본값 제거)
ENV SERVER_PORT=8080

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
