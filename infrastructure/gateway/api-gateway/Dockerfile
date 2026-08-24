FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Les POM sont copiés séparément et le dépôt Maven BuildKit persiste entre les
# builds : une modification de code ne télécharge donc pas les dépendances.
COPY pom.xml ./pom.xml
COPY infrastructure/gateway/api-gateway/pom.xml infrastructure/gateway/api-gateway/pom.xml
COPY services/auth/pom.xml services/auth/pom.xml
COPY services/account/pom.xml services/account/pom.xml
COPY services/notification/pom.xml services/notification/pom.xml
COPY infrastructure/gateway/api-gateway/src infrastructure/gateway/api-gateway/src
RUN --mount=type=cache,id=hopital-maven-repository,target=/root/.m2,sharing=locked \
    mvn --batch-mode --no-transfer-progress -pl infrastructure/gateway/api-gateway -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/infrastructure/gateway/api-gateway/target/*.jar app.jar
EXPOSE 8088
ENTRYPOINT ["java", "-jar", "app.jar"]
