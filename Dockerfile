# stage 1: builder
FROM gradle:8.5.0-jdk17 AS builder
WORKDIR /workspace
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew clean bootJar --no-daemon

# stage 2: runtime
FROM amazoncorretto:17-alpine-jdk
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# 8080(http)과 8443(https) 포트를 모두 노출
EXPOSE 8080 8443

# 컨테이너 시작 시 실행될 명령어
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
