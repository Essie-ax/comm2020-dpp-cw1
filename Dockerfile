FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package
RUN mvn -q -DskipTests dependency:copy-dependencies -DoutputDirectory=/app/target/dependency

FROM eclipse-temurin:17-jre
WORKDIR /app

# App classes/resources
COPY --from=build /app/target/classes /app/target/classes
# Dependency jars
COPY --from=build /app/target/dependency /app/target/dependency

EXPOSE 8080
ENV PORT=8080

# Run the main class directly (no need for executable jar manifest)
CMD ["sh","-c","java -cp '/app/target/classes:/app/target/dependency/*' uk.ac.comm2020.WebApp"]
