FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml /app/
COPY aspect-starter /app/aspect-starter/
COPY application-task /app/application-task/

RUN mvn -f /app/pom.xml clean install -DskipTests

RUN mvn -f /app/application-task/pom.xml clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/application-task/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
