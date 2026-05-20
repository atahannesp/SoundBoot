FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:25-jdk-alpine

RUN apk update && \
    apk add --no-cache ffmpeg

WORKDIR /app

RUN mkdir -p uploads/hls

COPY --from=build /app/target/SoundBoot-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
