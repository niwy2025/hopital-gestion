# Hopital Gestion

Monorepo de départ pour une architecture microservices de gestion d'hôpitaux.
Le dépôt fournit le socle d'infrastructure, un API Gateway Spring Boot, un
service `auth` squelette, la documentation API et une collection Postman.

## Arborescence principale

```text
docs/api/                         Documentation API par domaine
infrastructure/docker/            Scripts et fichiers Docker transverses
infrastructure/gateway/api-gateway/ Gateway Spring Boot / Spring Cloud Gateway
infrastructure/keycloak/          Realm Keycloak hospital
infrastructure/kong/              Notes et commandes Kong Admin API
infrastructure/monitoring/        Prometheus, Grafana, dashboards et provisioning
postman/                          Collections Postman
services/account/                 Microservice de gestion des comptes
services/auth/                    Microservice d'authentification
services/notification/            Microservice asynchrone e-mail et SMS
```

## Briques incluses

- **Spring Boot** pour les services Java (`api-gateway`, `auth-service`, `account-service`, `notification-service`).
- **Docker Compose** pour orchestrer les dépendances et services locaux.
- **Kong** comme API Gateway publique.
- **Keycloak** pour OAuth2/OpenID Connect et l'émission des JWT.
- **SQL Server** comme persistance principale des services.
- **Kafka** pour les échanges asynchrones.
- **Redis** pour le cache et le rate limiting.
- **Prometheus + Grafana** pour les métriques et dashboards.
- **Postman** pour tester les endpoints exposés.

## Démarrage rapide

```bash
cp .env.example .env
docker compose up -d
```

Ports utiles :

| Service | URL / port | Usage |
| --- | --- | --- |
| Kong proxy | `http://localhost:8000` | Point d'entrée API public |
| Kong Admin API | `http://localhost:8001` | Administration de Kong |
| API Gateway Spring | `http://localhost:8088` | Gateway applicatif interne |
| Auth Service | `http://localhost:8081` | Service d'authentification |
| Account Service | `http://localhost:8082` | Gestion des comptes, rôles et permissions |
| Notification Service | `http://localhost:8083` | Traitement asynchrone des e-mails et SMS |
| Keycloak | `http://localhost:8080` | Console IAM et endpoints OIDC |
| SQL Server | `localhost:1433` | Base de données applicative |
| Kafka | `localhost:9092` | Broker accessible depuis l'hôte |
| Redis | `localhost:6379` | Cache accessible depuis l'hôte |
| Prometheus | `http://localhost:9090` | Métriques |
| Grafana | `http://localhost:3000` | Dashboards |

## Convention de structure des services

Chaque microservice Java doit reprendre cette structure :

```text
src/main/java/.../
  api/                  Controllers REST
  application/
    config/             Configuration applicative
    domain/             Modèle métier
    dto/                Objets d'entrée/sortie
    exception/          Exceptions métier
    service/            Cas d'usage
  infra/
    cache/              Cache Redis
    config/             Configuration technique
    integration/        Clients externes
    security/           Sécurité et JWT
Dockerfile
README.md
```

## Migrations de base de données

Chaque service qui possède une base de données conserve ses migrations SQL dans
`src/main/resources/db/migration`. Les fichiers suivent le format
`V<version>_<nom-bref-de-la-migration>.sql`, par exemple
`V1_create_accounts.sql`. Les versions sont strictement croissantes et une
migration déjà appliquée ne doit jamais être modifiée.

Le service `services/account` applique déjà cette convention et servira de modèle
pour les futurs services hospitaliers comme `patient-service`,
`appointment-service`, `staff-service`, `billing-service` ou
`notification-service`.

## Observabilité

Prometheus collecte Kong, Keycloak, `api-gateway`, `auth-service`,
`account-service` et `notification-service` depuis
`infrastructure/monitoring/prometheus/prometheus.yml`. Chaque service Spring
Boot expose `/actuator/prometheus` grâce à Actuator et Micrometer, avec le tag
`application` pour distinguer ses métriques. Grafana provisionne la datasource
Prometheus et le dashboard **Hospital services overview**, qui affiche l'état
des cibles surveillées, le débit HTTP et la mémoire JVM par service.

## Documentation API et tests manuels

La documentation initiale se trouve dans `docs/api`. Les collections Postman
sont séparées par service dans `postman/collections` et partagent
l'environnement local `postman/environments/local.postman_environment.json`.

## Notifications asynchrones

Le topic Kafka `hospital.notification.request.v1` est la file d'attente des
notifications. Un service métier publie un message avec `sourceService`,
`type`, `channels`, `recipients`, `subject`, `body` et `metadata`, puis
`notification-service` se charge de l'envoi e-mail ou SMS hors du chemin HTTP.
Par exemple, `account-service` publie un e-mail de bienvenue après la création
confirmée d'un compte. L'endpoint public
`POST /api/v1/notifications/broadcasts` renvoie `202 Accepted` puis suit le
même flux Kafka.
