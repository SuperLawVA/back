# stage 1: builder
FROM gradle:8.5.0-jdk17 AS builder
WORKDIR /workspace
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew clean bootJar --no-daemon

# stage 2: runtime
FROM amazoncorretto:17-alpine-jdk

# 네트워크 진단을 위한 curl 설치
RUN apk update && apk add --no-cache curl

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

# 8080(http) 포트 노출
EXPOSE 8080

# 컨테이너 시작 시 실행될 명령어
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
