FROM openjdk:22

WORKDIR /app

# Copy the Maven wrapper and pom.xml first to leverage Docker cache
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make Maven wrapper executable
RUN chmod +x ./mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw package -DskipTests

# Expose the port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "target/warehouse-0.0.1-SNAPSHOT.jar"]

