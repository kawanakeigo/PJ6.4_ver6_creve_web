FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN mvn -B clean verify
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN groupadd --system creve && useradd --system --gid creve creve
COPY --from=build /workspace/app/target/app-1.0.0-SNAPSHOT.jar /app/application.jar
USER creve
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/application.jar"]
