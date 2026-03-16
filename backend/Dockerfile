## Build stage: compile the Spring Boot application with Maven
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy Maven descriptor and download dependencies first (cache-friendly)
COPY pom.xml .
RUN mvn -q dependency:go-offline

# Copy the source code and build the jar
COPY src ./src
RUN mvn -q -DskipTests clean package

## Run stage: run the built jar on a slim JDK image
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Copy the jar from the build stage
COPY --from=build /app/target/phishing-chat-backend-0.0.1-SNAPSHOT.jar app.jar

# Expose the port used by the Spring Boot app
EXPOSE 8080

# Default entrypoint
ENTRYPOINT ["java", "-jar", "app.jar"]

