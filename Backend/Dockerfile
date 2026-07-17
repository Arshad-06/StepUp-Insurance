# Stage 1: Build the application using Maven
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
# Package the application, skipping tests to speed up the deploy
RUN mvn clean package -DskipTests

# Stage 2: Run the application using JDK 21
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8765
ENTRYPOINT ["java", "-jar", "app.jar"]