# Auth Service

Microservice d'authentification pour la plateforme de gestion d'hôpitaux. Il
encapsulera les échanges avec Keycloak et exposera les endpoints `/api/v1/auth`.

## Structure standard

- `api` : contrôleurs REST.
- `application/config` : propriétés et configuration applicative.
- `application/domain` : objets du domaine.
- `application/dto` : contrats d'entrée/sortie.
- `application/exception` : exceptions métier.
- `application/service` : cas d'usage applicatifs.
- `infra/cache` : configuration cache Redis.
- `infra/config` : configuration technique.
- `infra/integration` : clients externes, dont Keycloak.
- `infra/security` : sécurité Spring Security / JWT.
