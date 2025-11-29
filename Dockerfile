# STAGE 1: Build
FROM eclipse-temurin:17-jdk-alpine AS build  # Start with Java 17 image, name this stage "build"
WORKDIR /app                                  # Set working directory inside container
COPY . .                                      # Copy all project files into container
RUN ./mvnw clean package -DskipTests          # Build the jar file, skip tests

# STAGE 2: Run
FROM eclipse-temurin:17-jre-alpine            # Fresh image with just JRE (smaller)
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar   # Copy jar from build stage
EXPOSE 8080                                   # Document that app uses port 8080
ENTRYPOINT ["java", "-jar", "app.jar"]        # Command to run the app