# Use a base image with Java and Alpine Linux
FROM openjdk:21

# Set the working directory in the container
WORKDIR /app

# Copy the JAR file from your local machine into the container
COPY target/pronoplayer-1.0.0-SNAPSHOT.jar /app/

# Expose the port that your Spring Boot application runs on
EXPOSE 8080

# Command to run your Spring Boot application
CMD ["java", "-jar", "pronoplayer-1.0.0-SNAPSHOT.jar"]
