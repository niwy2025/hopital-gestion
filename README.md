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
services/auth/                    Microservice d'authentification
```

## Briques incluses

- **Spring Boot** pour les services Java (`api-gateway`, `auth-service`).
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

Le service `services/auth` applique déjà cette convention et servira de modèle
pour les futurs services hospitaliers comme `patient-service`,
`appointment-service`, `staff-service`, `billing-service` ou
`notification-service`.

## Observabilité

Prometheus scrape Kong, Keycloak, `api-gateway` et `auth-service` depuis
`infrastructure/monitoring/prometheus/prometheus.yml`. Grafana provisionne la
datasource Prometheus et un dashboard initial depuis
`infrastructure/monitoring/grafana`.

## Documentation API et tests manuels

La documentation initiale se trouve dans `docs/api`. La collection Postman est
fournie dans `postman/hopital-gestion.postman_collection.json`.
