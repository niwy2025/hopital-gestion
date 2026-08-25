FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml ./pom.xml
COPY infrastructure/gateway/api-gateway/pom.xml infrastructure/gateway/api-gateway/pom.xml
COPY services/auth/pom.xml services/auth/pom.xml
COPY services/account/pom.xml services/account/pom.xml
COPY services/notification/pom.xml services/notification/pom.xml
COPY services/organization/pom.xml services/organization/pom.xml
COPY services/laboratory/pom.xml services/laboratory/pom.xml
COPY services/patient/pom.xml services/patient/pom.xml
COPY services/personnel/pom.xml services/personnel/pom.xml

RUN --mount=type=cache,id=hopital-maven-repository,target=/root/.m2,sharing=locked \
    mvn --batch-mode --no-transfer-progress -pl services/laboratory -am dependency:go-offline

COPY services/laboratory/src services/laboratory/src
RUN --mount=type=cache,id=hopital-maven-repository,target=/root/.m2,sharing=locked \
    mvn --batch-mode --no-transfer-progress -pl services/laboratory -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/services/laboratory/target/*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
