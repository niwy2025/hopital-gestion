FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Le dépôt Maven BuildKit persiste entre les builds : une modification de code
# ne télécharge donc pas les dépendances déjà résolues.
COPY pom.xml ./pom.xml
COPY infrastructure/gateway/api-gateway/pom.xml infrastructure/gateway/api-gateway/pom.xml
COPY services/auth/pom.xml services/auth/pom.xml
COPY services/account/pom.xml services/account/pom.xml
COPY services/notification/pom.xml services/notification/pom.xml
COPY services/organization/pom.xml services/organization/pom.xml

# Cette couche ne change que si un POM évolue. Docker réutilise alors toutes
# les dépendances déjà résolues pour une simple modification de code.
RUN --mount=type=cache,id=hopital-maven-repository,target=/root/.m2,sharing=locked \
    mvn --batch-mode --no-transfer-progress -pl services/organization -am dependency:go-offline

COPY services/organization/src services/organization/src
RUN --mount=type=cache,id=hopital-maven-repository,target=/root/.m2,sharing=locked \
    mvn --batch-mode --no-transfer-progress -pl services/organization -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/services/organization/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
