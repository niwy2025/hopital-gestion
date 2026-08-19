FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml ./pom.xml
COPY infrastructure/gateway/api-gateway/pom.xml infrastructure/gateway/api-gateway/pom.xml
COPY services/auth/pom.xml services/auth/pom.xml
COPY services/account/pom.xml services/account/pom.xml
COPY services/account/src services/account/src
RUN mvn -pl services/account -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/services/account/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
