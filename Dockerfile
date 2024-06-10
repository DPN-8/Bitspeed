FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

EXPOSE = 8000
FROM openjdk:17-jdk-slim
COPY --from=build /target/fluxcart-0.0.1-SNAPSHOT.jar fluxcart.jar
ENTRYPOINT ["java", "-jar", "fluxcart.jar"]
