FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Les POM sont copiés séparément et le dépôt Maven BuildKit persiste entre les
# builds : une modification de code ne télécharge donc pas les dépendances.
COPY pom.xml ./pom.xml
COPY infrastructure/gateway/api-gateway/pom.xml infrastructure/gateway/api-gateway/pom.xml
COPY services/auth/pom.xml services/auth/pom.xml
COPY services/account/pom.xml services/account/pom.xml
COPY services/notification/pom.xml services/notification/pom.xml
COPY services/organization/pom.xml services/organization/pom.xml
COPY services/laboratory/pom.xml services/laboratory/pom.xml
COPY services/patient/pom.xml services/patient/pom.xml

# Cette couche ne change que si un POM évolue. Docker réutilise alors toutes
# les dépendances déjà résolues pour une simple modification de code.
RUN --mount=type=cache,id=hopital-maven-repository,target=/root/.m2,sharing=locked \
    mvn --batch-mode --no-transfer-progress -pl services/auth -am dependency:go-offline

COPY services/auth/src services/auth/src
RUN --mount=type=cache,id=hopital-maven-repository,target=/root/.m2,sharing=locked \
    mvn --batch-mode --no-transfer-progress -pl services/auth -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/services/auth/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
