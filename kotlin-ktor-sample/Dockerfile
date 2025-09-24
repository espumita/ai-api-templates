# Build stage
FROM gradle:9.0.0-jdk21 AS build
WORKDIR /app
COPY . .
RUN ./gradlew buildFatJar --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre AS final
WORKDIR /app
COPY --from=build /app/build/libs/*-all.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
