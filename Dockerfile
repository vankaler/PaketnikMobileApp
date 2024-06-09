# Gradle build stage
FROM gradle:6.7.1-jdk11 AS build

WORKDIR /app

COPY . .

RUN gradle build --no-daemon

# Runtime stage
FROM openjdk:11-jre-slim

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 3002

ENTRYPOINT ["java", "-jar", "app.jar"]
