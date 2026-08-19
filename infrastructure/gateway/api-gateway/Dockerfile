FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Cette couche ne change que lorsque les POM changent : les dépendances Maven
# sont ainsi réutilisées lors d'une modification du code source.
COPY pom.xml ./pom.xml
COPY infrastructure/gateway/api-gateway/pom.xml infrastructure/gateway/api-gateway/pom.xml
COPY services/auth/pom.xml services/auth/pom.xml
COPY services/account/pom.xml services/account/pom.xml
RUN --mount=type=cache,id=hopital-maven-repository,target=/root/.m2,sharing=locked \
    for attempt in 1 2 3; do \
      mvn --batch-mode --no-transfer-progress -Dmaven.wagon.http.retryHandler.count=5 dependency:go-offline && exit 0; \
    done; \
    exit 1

COPY infrastructure/gateway/api-gateway/src infrastructure/gateway/api-gateway/src
RUN --mount=type=cache,id=hopital-maven-repository,target=/root/.m2,sharing=locked \
    mvn --batch-mode --no-transfer-progress -pl infrastructure/gateway/api-gateway -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/infrastructure/gateway/api-gateway/target/*.jar app.jar
EXPOSE 8088
ENTRYPOINT ["java", "-jar", "app.jar"]
