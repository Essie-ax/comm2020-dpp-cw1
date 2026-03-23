FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src
COPY db ./db
COPY docs ./docs
COPY .mvn ./.mvn
COPY mvnw.cmd ./
COPY README.md ./

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/comm2020-dpp-cw1-0.1.0.jar app.jar

ENV PORT=10000
EXPOSE 10000

CMD ["java", "-jar", "app.jar"]